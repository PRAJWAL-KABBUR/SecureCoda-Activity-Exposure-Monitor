 /* ============================
       Config & state
       ============================ */
    let currentPage = 0;          // zero-based page index used by backend
    let pageSize = 5;
    let totalPages = 0;
    let totalElements = 0;
    let currentSort = 'detectedAt';
    let currentDir = 'desc';
    let currentSearch = '';
    const searchDebounceMs = 300;
    let searchTimer = null;

    /* ============================
       Helpers
       ============================ */
    function safeEncode(s) {
      if (s === null || s === undefined) return '';
      return String(s)
        .replaceAll('&','&amp;')
        .replaceAll('<','&lt;')
        .replaceAll('>','&gt;')
        .replaceAll('"','&quot;')
        .replaceAll("'",'&#39;');
    }

    function setLastUpdated() {
      document.getElementById('lastUpdated').innerText = 'Updated: ' + new Date().toLocaleString();
    }

    /* Shows a simple placeholder when empty */
    function showEmpty(containerId, text) {
      document.getElementById(containerId).innerHTML = `<div class="text-slate-500 py-6 text-center">${safeEncode(text)}</div>`;
    }

    /* Build query param string for alerts */
    function buildAlertsUrl() {
      const sortParam = `${currentSort},${currentDir}`;
      const q = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        search: currentSearch || ''
      });
      return '/api/alerts?' + q.toString();
    }

    /* ============================
       Fetch & render alerts
       ============================ */
    async function loadAlerts() {
      try {
        const url = buildAlertsUrl();
        const res = await fetch(url);
        if (!res.ok) {
          showEmpty('alertsContainer', `Failed to load alerts: ${res.status} ${res.statusText}`);
          return;
        }

        const data = await res.json();
        // backend returns Page<AlertEntity> => data.content
        const alerts = data.content || [];
        totalPages = (typeof data.totalPages === 'number') ? data.totalPages : Math.ceil((data.totalElements || alerts.length) / pageSize);
        totalElements = data.totalElements || alerts.length;

        if (!alerts.length) {
          showEmpty('alertsContainer', 'No alerts found for this query.');
          renderPaginationControls();
          return;
        }

        // build table
        let html = `<div class="overflow-x-auto"><table class="w-full text-sm border-collapse">
          <thead class="bg-slate-50 text-slate-700"><tr>
            <th class="p-3 text-left">ID</th>
            <th class="p-3 text-left">Document</th>
            <th class="p-3 text-left">Type</th>
            <th class="p-3 text-left">Details</th>
            <th class="p-3 text-left">Detected</th>
            <th class="p-3 text-left">Actions</th>
          </tr></thead><tbody>`;

        for (const a of alerts) {
          const details = typeof a.details === 'string' ? a.details : JSON.stringify(a.details || {});
          html += `<tr class="border-b">
            <td class="p-3 align-top">${safeEncode(a.id)}</td>
            <td class="p-3 align-top">${safeEncode(a.docName)} <span class="text-slate-400">(${safeEncode(a.docId)})</span></td>
            <td class="p-3 align-top">${safeEncode(a.type)}</td>
            <td class="p-3 align-top">
  <div class="alert-json-box">
    ${safeEncode(details)}
  </div>
</td>
            <td class="p-3 align-top text-slate-500">${a.detectedAt ? new Date(a.detectedAt).toLocaleString() : ''}</td>
            <td class="p-3 align-top">
              <button class="px-2 py-1 bg-green-600 text-white rounded text-sm" onclick="resolveAlert(${a.id})">Resolve</button>
            </td>
          </tr>`;
        }

        html += `</tbody></table></div>`;
        document.getElementById('alertsContainer').innerHTML = html;

        renderPaginationControls();
        setLastUpdated();

      } catch (err) {
        showEmpty('alertsContainer', 'Error loading alerts: ' + (err.message || err));
      }
    }

    /* Resolve action (keeps you on same page) */
    async function resolveAlert(id) {
      try {
        const res = await fetch(`/api/alerts/${id}/resolve`, { method: 'POST' });
        if (!res.ok) {
          alert('Failed to resolve: ' + res.statusText);
          return;
        }
        // reload current page after resolving
        await loadAlerts();
      } catch (e) {
        alert('Error: ' + e.message);
      }
    }

    /* ============================
       Pagination controls
       ============================ */
    function renderPaginationControls() {
      const pageInfo = document.getElementById('pageInfo');
      pageInfo.innerText = `Page ${currentPage + 1} of ${Math.max(1, totalPages)} · ${totalElements} items`;

      const prevBtn = document.getElementById('prevPageBtn');
      const nextBtn = document.getElementById('nextPageBtn');
      const firstBtn = document.getElementById('firstPageBtn');
      const lastBtn = document.getElementById('lastPageBtn');

      prevBtn.disabled = (currentPage <= 0);
      firstBtn.disabled = (currentPage <= 0);
      nextBtn.disabled = (currentPage >= totalPages - 1);
      lastBtn.disabled = (currentPage >= totalPages - 1);
    }

    function goToPage(delta) {
      const newPage = Math.min(Math.max(0, currentPage + delta), Math.max(0, totalPages - 1));
      if (newPage === currentPage) return;
      currentPage = newPage;
      loadAlerts();
    }

    function goToFirst() {
      if (currentPage === 0) return;
      currentPage = 0;
      loadAlerts();
    }
    function goToLast() {
      if (currentPage >= totalPages - 1) return;
      currentPage = Math.max(0, totalPages - 1);
      loadAlerts();
    }

    /* ============================
       Controls wiring
       ============================ */
    document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
      pageSize = parseInt(e.target.value, 10) || 20;
      currentPage = 0;
      loadAlerts();
    });

    document.getElementById('prevPageBtn').addEventListener('click', () => goToPage(-1));
    document.getElementById('nextPageBtn').addEventListener('click', () => goToPage(1));
    document.getElementById('firstPageBtn').addEventListener('click', goToFirst);
    document.getElementById('lastPageBtn').addEventListener('click', goToLast);
    document.getElementById('refreshBtn').addEventListener('click', () => {
    currentPage = 0;      // reset to first page
    loadAlerts();         // reload alerts
    setLastUpdated();
});

    /* Debounced search */
    document.getElementById('searchInput').addEventListener('input', (e) => {
      currentSearch = e.target.value.trim();
      currentPage = 0;
      if (searchTimer) clearTimeout(searchTimer);
      searchTimer = setTimeout(() => {
        loadAlerts();
        searchTimer = null;
      }, searchDebounceMs);
    });

    /* ============================
       Documents loader (Option C)
       We'll request a large page size and render data.content if available
       ============================ */
    async function loadDocs() {
      try {
        // request big page (docs typically fewer); backend returns Page => data.content
        const res = await fetch('/api/docs?page=0&size=1000');
        if (!res.ok) {
          document.getElementById('docsContainer').innerText = 'Failed to load docs';
          return;
        }
        const data = await res.json();
        const docs = data.content || data; // if backend changed, be resilient

        if (!docs.length) {
          document.getElementById('docsContainer').innerHTML = '<div class="text-slate-500">No documents found</div>';
          return;
        }

        let html = `<div class="overflow-x-auto"><table class="w-full text-sm border-collapse">
          <thead class="bg-slate-50 text-slate-700"><tr>
            <th class="p-3 text-left">ID</th>
            <th class="p-3 text-left">Name</th>
            <th class="p-3 text-left">Published</th>
            <th class="p-3 text-left">Updated</th>
          </tr></thead><tbody>`;

        for (const d of docs) {
          html += `<tr class="border-b">
            <td class="p-3">${safeEncode(d.id)}</td>
            <td class="p-3">${safeEncode(d.name)}</td>
            <td class="p-3">${safeEncode(d.published)}</td>
            <td class="p-3 text-slate-500">${d.updatedAt ? new Date(d.updatedAt).toLocaleString() : ''}</td>
          </tr>`;
        }

        html += `</tbody></table></div>`;
        document.getElementById('docsContainer').innerHTML = html;

      } catch (e) {
        document.getElementById('docsContainer').innerText = 'Error loading documents: ' + e.message;
      }
    }

    /* ============================
       Initialization
       ============================ */
    async function init() {
      // set page size select initial value from variable
      document.getElementById('pageSizeSelect').value = String(pageSize);

      await Promise.all([loadAlerts(), loadDocs()]);
      setLastUpdated();
    }

    init();