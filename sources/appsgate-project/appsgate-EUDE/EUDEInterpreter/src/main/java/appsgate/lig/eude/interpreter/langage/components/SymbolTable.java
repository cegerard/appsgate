package appsgate.lig.eude.interpreter.langage.components;

import java.util.HashMap;

public class SymbolTable {

    private final HashMap<String, Element> symbols;

    /**
     * Constructor
     */
    public SymbolTable() {
        symbols = new HashMap<String, Element>();
    }

    /**
     *
     * @param id
     * @param type
     */
    public void add(String id, String type) {
        Element e = new Element(id, type);
        if (this.getElementKey(e) == null) {
            String keyVal = type.substring(0, 3) + "_" + symbols.size();
            symbols.put(keyVal, e);
        }
    }

    public void addElement(String varName, String id, String type) {
        symbols.put(varName, new Element(id, type));
    }

    public String getElementKey(Element l) {
        for (String k : symbols.keySet()) {
            if (symbols.get(k).equals(l)) {
                return k;
            }
        }
        return null;
    }

    public String getElementKey(String id, String type) {
        return getElementKey(new Element(id, type));
    }

    public Element getElementByKey(String key) {
        return symbols.get(key);
    }

    public String getExpertProgramDecl() {
        String ret = "";
        for (String k : symbols.keySet()) {
            ret += k + " = " + symbols.get(k).getExpertProgramDecl() + "\n";
        }
        return ret;
    }

    public class Element {

        private final String id;
        private final String type;

        public Element(String i, String t) {
            this.id = i;
            this.type = t;
        }

        public String getExpertProgramDecl() {
            return "{ type: " + this.type + ", id: " + this.id + "}";
        }

        public boolean equals(Element other) {
            return other.id.equals(this.id) && other.type.equals(this.type);
        }

    }
}
