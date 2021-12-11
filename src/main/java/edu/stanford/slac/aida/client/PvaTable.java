package edu.stanford.slac.aida.client;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PvaTable {
    List<String> labels = new ArrayList<String>();
    List<String> descriptions = new ArrayList<String>();
    List<String> units = new ArrayList<String>();
    Map<String, List<Object>> values = new HashMap<String, List<Object>>();
}
