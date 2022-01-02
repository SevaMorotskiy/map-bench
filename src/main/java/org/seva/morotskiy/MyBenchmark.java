/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

/***
 *
 *  Java  - TreeMap vs HashMap
 */


package org.seva.morotskiy;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@State(Scope.Thread)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)


@BenchmarkMode(Mode.AverageTime)
@Fork(1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)


public class MyBenchmark {
    // Key Map Object
    private class KeyMapObj implements Comparable <KeyMapObj> {
        Integer key;

        public KeyMapObj(Integer key) {
            this.key = key;
        }

        public Integer getKey() {
            return key;
        }
        public int getKeyValue() {
            return key != null ? key.intValue() : 0;
        }

        @Override
        public boolean equals(Object o) {
            return key.equals(o);
        }

        @Override
        public int hashCode() {
            //Objects.hash(key);
            return getKey().hashCode();
        }

        @Override
        public int compareTo(KeyMapObj o) {
            int diff = this.key - o.key;

            return  diff;
        }
    }

    // String value for TreeMap and HashMap
    public static final String MAP_ELEMENT = "MyBenchmark App for \"TreeMap vs HashMap comparison\"";
    // Debug mode flag
    public static final boolean DEBUG = Boolean.FALSE.booleanValue();
    // Random TreeMap key
    public static KeyMapObj randTreeMapKey = null;
    // Random HashMap key
    public static KeyMapObj randHashMapKey = null;


    /**
     * In many cases, the experiments require walking the configuration space
     * for a benchmark. This is needed for additional control, or investigating
     * how the workload performance changes with different settings.
     */
    @Param ({"10"})
    private int capacity;

    private Map<KeyMapObj, String> tree;
    private Map<KeyMapObj, String> map;

    @Setup(Level.Invocation)
    public void doSetup() {
        // Populate a treemap collection
        Stream<KeyMapObj> treeMapStream = new Random().ints(capacity).boxed().map(a -> new KeyMapObj(a));
        //treeMapStream.forEach(key -> tree.put(key, MAP_ELEMENT));
        tree = treeMapStream.collect(
                Collectors.toMap(
                        Function.identity(), /* a -> a*/
                        (ignored) -> MAP_ELEMENT,
                        (oldValue, newValue) -> oldValue, /*mergeFunction*/
                        TreeMap::new
                )
        );
        // Input random key into TreeMap
        Random randTreeMap = new Random();
        randTreeMapKey  = new KeyMapObj(randTreeMap.nextInt());
        tree.put(randTreeMapKey, MAP_ELEMENT);



        // Populate a hashmap collection
        Stream<KeyMapObj> hashMapStream = new Random().ints(capacity).boxed().map(a -> new KeyMapObj(a));
        //hashMapStream.forEach(key -> map.put(key, MAP_ELEMENT));
        map = hashMapStream.collect(
                Collectors.toMap(
                        Function.identity(), /* a -> a*/
                        (ignored) -> MAP_ELEMENT,
                        (oldValue, newValue) -> oldValue, /*mergeFunction*/
                        () -> new HashMap((int) ((capacity / .75) + 1))
                )
        );
        // Input random key into HashMap
        Random randHashMap = new Random();
        randHashMapKey  = new KeyMapObj(randHashMap.nextInt());
        map.put(randHashMapKey, MAP_ELEMENT);

        printOut();

    }

    @TearDown(Level.Invocation)
    public void doTearDown() {
        tree = new TreeMap<KeyMapObj, String>();
        map = new HashMap<KeyMapObj, String>();
    }


    @Benchmark
    public void doTreeMap() {
        tree.get(randTreeMapKey);
        return;
    }

    @Benchmark
    public void doHashMap() {
        map.get(randHashMapKey);
        return;
    }

    private void printOut() {
         if (!DEBUG) {
            return;
         }

        // Printing out the treeMap collection
        for (Map.Entry<KeyMapObj,String> entry : tree.entrySet())
        {
            KeyMapObj variableKey   = entry.getKey();
            String variableValue    = entry.getValue();

            System.out.println("[treeMap] Name: " + variableKey.getKeyValue());
            System.out.println("[treeMap] Value: " + variableValue);
        }

        // Printing out the hashMap collection
        for (KeyMapObj key : map.keySet())
        {
            KeyMapObj variableKey = key;
            String variableValue = map.get(key);

            System.out.println("[hashMap] Name: " + variableKey.getKeyValue());
            System.out.println("[hashMap] Value: " + variableValue);
        }
    }



    public static void main (String[] args) throws RunnerException {
        Options options = new OptionsBuilder().include(MyBenchmark.class.getSimpleName())
                                .jvmArgs("-Xms1G", "-Xmx2G")
                                .build();

        new Runner(options).run();
    }

}
