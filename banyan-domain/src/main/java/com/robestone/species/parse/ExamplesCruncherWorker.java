package com.robestone.species.parse;

public class ExamplesCruncherWorker extends AbstractWorker {

	public static void main(String[] args) {
		new ExamplesCruncherWorker().run();
	}
	
	public void run() {
		examplesService.crunchIds();
	}
	
}
