/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iiit.ire.mp;

import gate.*;
import gate.creole.*;
import gate.util.*;

import java.io.File;
import java.io.IOException;
/**
 *
 * @author messi
 */
public class GATESingleton {

    private static GATESingleton instance = null;
    public static GATEImplementation annie;
    public static Corpus corpus; 
	/** The Corpus Pipeline application to contain ANNIE */
    public static SerialAnalyserController annieController;
    
    protected GATESingleton() throws GateException,IOException{
        Out.prln("Initialising GATE...");
        File gateHome = new File("//usr//local//GATE_Developer_7.0");
        Gate.setGateHome(gateHome);
        Gate.init();
        // Load ANNIE plugin//
        gateHome = Gate.getGateHome();

        File pluginsHome = new File(gateHome, "plugins");
        Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURL());
        Out.prln("...GATE initialised");

        // initialise ANNIE (this may take several minutes)
        initAnnie();
        corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
        
    }

    public static synchronized GATESingleton getInstance() throws GateException,IOException{
        if (instance == null) {
            instance = new GATESingleton();
        }
        return instance;
    }
    
    public void initAnnie() throws GateException {
		// //Out.prln("Initialising ANNIE...");

		// create a serial analyser controller to run ANNIE with
		annieController = (SerialAnalyserController) Factory.createResource(
				"gate.creole.SerialAnalyserController",
				Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_"
						+ Gate.genSym());

		// load each PR as defined in ANNIEConstants
		for (int i = 0; i < ANNIEConstants.PR_NAMES.length; i++) {
			FeatureMap params = Factory.newFeatureMap(); // use default
															// parameters
			ProcessingResource pr = (ProcessingResource) Factory
					.createResource(ANNIEConstants.PR_NAMES[i], params);

			// add the PR to the pipeline controller
			annieController.add(pr);
		} // for each ANNIE PR

		// Out.prln("...ANNIE loaded");
	} // initAnnie()

    
    
    
}