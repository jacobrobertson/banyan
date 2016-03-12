package com.robestone.species.search;

import java.io.IOException;

import junit.framework.TestCase;

public abstract class AbstractSearcherTester extends TestCase {
	
	public void testSearcherQuery1() throws IOException {
		doTestSearcherQuery("grassy snakes", "Natrix natrix");
		doTestSearcherQuery("bolivean aniconda", "Eunectes beniensis");
		doTestSearcherQuery("french anacondas", "Eunectes francois");
		doTestSearcherQuery("anicondas", "Eunectes francois");
		doTestSearcherQuery("aniconda", "Eunectes francois");
		
		doTestSearcherQuery("friend", "F");
		doTestSearcherQuery("castor", "Castor");
		doTestSearcherQuery("prare dogs", "p1");

	}
	public void testArcticWhale() throws IOException {
		doTestSearcherQuery("Arctic Whale", "Balaena");
	} 
	public void testRabbit() throws IOException {
		doTestSearcherQuery("rabbit", "Pentalagus", "Trifolium arvense", "Leporidae");
	} 
	public void testLions() throws IOException {
		doTestSearcherQuery("lion", "Panthera Leo");
		doTestSearcherQuery("lions", "Panthera Leo");
	} 
	public void testAnacondas() throws IOException {
		doTestSearcherQuery("anicondas", "Eunectes francois");
	}
	public void testWedgefish() throws IOException {
		doTestSearcherQuery("wedgefish", "Rhina");
	}
	public void testSearcherQuery2() throws IOException {
		doTestSearcherQuery("Tengmalm", "Aegolius funereus");
		doTestSearcherQuery("Tengmalm Owl", "Aegolius funereus");
		doTestSearcherQuery("Tengmalm s Owl", "Aegolius funereus");
		doTestSearcherQuery("Tengmalms Owl", "Aegolius funereus");
		doTestSearcherQuery("Tengmalm's Owl", "Aegolius funereus");
	}
	public void testGigantopithecus() throws IOException {
		doTestSearcherQuery("Gigantopithecus", "Gigantopithecus");
	}
	public void testLeontopithecus() throws IOException {
		doTestSearcherQuery("Leontopithecus", "Leontopithecus");
	}
	public void testBorealOwl() throws IOException {
		doTestSearcherQuery("Boreal owl", "Aegolius funereus");
	}
	public void testLangurs() throws IOException {
		doTestSearcherQuery("langurs", "Semnopithecus");
	}
	public void testRacoonBaboon() throws IOException {
		doTestSearcherQuery("racoon", "Procyon", "Procyonidae", "Procyon lotor", "Papio", "Ranodon", "Ranodon sibiricus", "Lacon punctatus");
		doTestSearcherQuery("racoons", "Procyon");
	}
	protected abstract Integer doTestSearcherQuery(String queryString, String... expectedLatinNames);
	
}
