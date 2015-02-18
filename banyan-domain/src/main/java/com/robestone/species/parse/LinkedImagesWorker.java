package com.robestone.species.parse;

public class LinkedImagesWorker extends AbstractWorker {

	public static void main(String[] args) {
		new LinkedImagesWorker().run();
	}
	public void run() {
		speciesService.updateLinkedImageIds();
	}
	
}
