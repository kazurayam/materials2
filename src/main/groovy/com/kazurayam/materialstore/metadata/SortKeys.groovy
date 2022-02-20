package com.kazurayam.materialstore.metadata

import com.google.gson.Gson

class SortKeys {

    public static final SortKeys NULL_OBJECT = new SortKeys(new ArrayList<String>())

    private final List<String> arguments

    SortKeys(String ... args) {
        this(Arrays.asList(args))
    }

    SortKeys(List<String> args) {
        this.arguments = args
    }

    Iterator<String> iterator() {
        arguments.iterator()
    }

    int size() {
        return arguments.size()
    }

    String get(int index) {
        return arguments.get(index)
    }

    @Override
    String toString() {
        Gson gson = new Gson()
        StringBuilder sb = new StringBuilder()
        sb.append("[")
        arguments.eachWithIndex { arg, index ->
            if (index > 0) {
                sb.append(", ")
            }
            sb.append(gson.toJson(arg))
        }
        sb.append("]")
        return sb.toString()
    }
}
