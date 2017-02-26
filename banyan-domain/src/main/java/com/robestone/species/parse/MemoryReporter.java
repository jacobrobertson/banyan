package com.robestone.species.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MemoryReporter {

	public static void main(String[] args) {
		MemoryReporter reporter = new MemoryReporter();
		Random ran = new Random();
	    Map<Integer, String> map = new HashMap<Integer, String>();
	    for (int i = 0; i < 200000000; i++) {
	    	reporter.report();
	        map.put(i, new Long(ran.nextLong()).toString());
	    }
	}
	
	private Runtime runtime = Runtime.getRuntime();
	
    private long currentTotalMemory = 0;
    private long currentFreeMrmory = 0;
	
	public void report() {

        long total = runtime.totalMemory();
        long free = runtime.freeMemory();

        if (total != currentTotalMemory || free != currentFreeMrmory) {
            System.out.println(
	                String.format("Memory Report > Total: %,d, Free: %,d, FreeChange: %,d",
	                    total,
	                    free,
	                    currentFreeMrmory - free));
            currentTotalMemory = total;
            currentFreeMrmory = free;
        }

	}
	
}
