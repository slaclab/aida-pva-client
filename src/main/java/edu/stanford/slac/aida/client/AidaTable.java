package edu.stanford.slac.aida.client;

import lombok.Data;
import org.epics.pvdata.pv.PVStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.tableResults;

@Data
public class AidaTable {
    List<String> labels = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    List<String> units = new ArrayList<>();
    Map<String, List<Object>> values = new HashMap<>();

    public static AidaTable from(PVStructure results) {
        return tableResults(results);
    }
}
