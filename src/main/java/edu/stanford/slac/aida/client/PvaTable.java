package edu.stanford.slac.aida.client;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PvaTable {
    List<String> labels = new ArrayList<>();
    List<String> descriptions = new ArrayList<>();
    List<String> units = new ArrayList<>();
    Map<String, List<Object>> values = new HashMap<>();
}
