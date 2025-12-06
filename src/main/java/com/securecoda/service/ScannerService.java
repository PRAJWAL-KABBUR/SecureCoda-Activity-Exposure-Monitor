package com.securecoda.service;
import com.securecoda.scanner.SensitivePatterns;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class ScannerService {
    public static class Finding {
        public final String columnId;
        public final String columnName;
        public final String type;
        public final String value;
        public final String pattern;
        public Finding(String columnId, String columnName, String type, String value, String pattern){
            this.columnId = columnId; this.columnName = columnName; this.type = type; this.value = value; this.pattern = pattern;
        }
    }
    private String detectType(Pattern p){
        String regex = p.pattern().toLowerCase();
        if (regex.contains("\\d{3}-\\d{2}-\\d{4}")) return "SSN";
        if (regex.contains("13,19") || regex.contains("(?:\\d[ -]*?)")) return "CC";
        if (regex.contains("password") || regex.contains("pwd") || regex.contains("secret")) return "PASSWORD";
        if (regex.contains("@")) return "EMAIL";
        return "SENSITIVE";
    }
    public List<Finding> scanTextStructured(String text){
        List<Finding> out = new ArrayList<>(); if (text==null) return out;
        for (Pattern p: SensitivePatterns.ALL){
            Matcher m = p.matcher(text);
            while (m.find()) out.add(new Finding(null, null, detectType(p), m.group(), p.pattern()));
        }
        return out;
    }
    public List<Finding> scanRowValuesStructured(Map<String,Object> values, Map<String,String> columnNames){
        List<Finding> out = new ArrayList<>();
        if (values==null) return out;
        for (Map.Entry<String,Object> e: values.entrySet()){
            String colId = e.getKey();
            String colName = columnNames != null ? columnNames.getOrDefault(colId, colId) : colId;
            String val = e.getValue() == null ? "" : e.getValue().toString();
            for (Pattern p: SensitivePatterns.ALL){
                Matcher m = p.matcher(val);
                while (m.find()) out.add(new Finding(colId, colName, detectType(p), m.group(), p.pattern()));
            }
        }
        return out;
    }
}
