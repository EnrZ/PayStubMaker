package dev.ez.PayStubMaker.data;

import dev.ez.PayStubMaker.models.Stub;

import java.util.HashMap;
import java.util.Map;

public class StubData {

    private static Map<Integer, Stub> stubs = new HashMap<>();


    public static Stub getById(int id){
        return stubs.get(id);
    }
}

