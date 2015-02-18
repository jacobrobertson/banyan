package com.robestone.species.parse;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.robestone.species.ExamplesService;
import com.robestone.species.ImageService;
import com.robestone.species.SpeciesService;

public class AbstractWorker {

	public AbstractWorker() {
		if (!inited) {
			String path = "com/robestone/species/parse/SpeciesServices.spring.xml";
			beanFactory = new ClassPathXmlApplicationContext(path);
			S_parseStatusService = (ParseStatusService) beanFactory.getBean("ParseStatusService");
			S_speciesService = (SpeciesService) beanFactory.getBean("SpeciesService");
			S_examplesService = (ExamplesService) beanFactory.getBean("ExamplesService");
			S_imageService = (ImageService) beanFactory.getBean("ImageService");
			inited = true;
		}
		this.parseStatusService = S_parseStatusService;
		this.speciesService = S_speciesService;
		this.examplesService = S_examplesService;
		this.imageService = S_imageService;
	}
	
	protected ParseStatusService parseStatusService;
	public SpeciesService speciesService;
	protected ExamplesService examplesService;
	protected ImageService imageService;
	
	private static boolean inited = false;
	protected static BeanFactory beanFactory;
	private static ParseStatusService S_parseStatusService;
	private static SpeciesService S_speciesService;
	private static ImageService S_imageService;
	private static ExamplesService S_examplesService;
	
	protected Object getBean(String name) {
		return beanFactory.getBean(name);
	}
	
}
