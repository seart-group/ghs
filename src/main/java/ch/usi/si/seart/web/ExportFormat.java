package ch.usi.si.seart.web;

public enum ExportFormat {

    CSV, JSON, XML;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
