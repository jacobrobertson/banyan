package com.robestone.banyan.workers;

public class ExamplesCruncherWorker extends AbstractWorker {

	public static void main(String[] args) {
		new ExamplesCruncherWorker().run();
	}
	
	public void run() {
		// I started just using latin, so I don't need to worry about updating the terms
		// NOTE - if this is run with "true", it will delete any "$", "!" from the DB
		examplesService.crunchIds(false);
	}
	
}
