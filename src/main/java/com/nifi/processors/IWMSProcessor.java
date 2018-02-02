package com.nifi.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractSessionFactoryProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessSessionFactory;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

@Tags({"SAP", "IWMS", "MM01"})
@CapabilityDescription("Run SAP T-code")
public class IWMSProcessor extends AbstractSessionFactoryProcessor  {
	private List<PropertyDescriptor> properties;
	private Set<Relationship> relationships;

		public static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
			.description("Successfully created Json File from data.").build();

	public static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
			.description("Failed to create Avro schema from data.").build();

	@Override
	protected void init(final ProcessorInitializationContext context) {
		final List<PropertyDescriptor> properties = new ArrayList<>();
		this.properties = Collections.unmodifiableList(properties);

		final Set<Relationship> relationships = new HashSet<>();
		relationships.add(REL_SUCCESS);
		relationships.add(REL_FAILURE);
		this.relationships = Collections.unmodifiableSet(relationships);
	}

	@Override
	protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return properties;
	}

	@Override
	public Set<Relationship> getRelationships() {
		return relationships;
	}

	@Override
	public void onTrigger(ProcessContext context, final ProcessSessionFactory sessionFactory) throws ProcessException {
		ScriptEngineManager factory = new ScriptEngineManager();
	    ScriptEngine scriptEngine = factory.getEngineByName("JavaScript");
//	    engine.eval("print('Hello, World')");
	    
		ProcessSession session = sessionFactory.createSession();
        try {

            try {
                Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
                if (bindings == null) {
                    bindings = new SimpleBindings();
                }
                bindings.put("session", session);
                bindings.put("context", context);
//                bindings.put("log", log);
                bindings.put("REL_SUCCESS", REL_SUCCESS);
                bindings.put("REL_FAILURE", REL_FAILURE);

                // Find the user-added properties and set them on the script
                for (Map.Entry<PropertyDescriptor, String> property : context.getProperties().entrySet()) {
                    if (property.getKey().isDynamic()) {
                        // Add the dynamic property bound to its full PropertyValue to the script engine
                        if (property.getValue() != null) {
                            bindings.put(property.getKey().getName(), context.getProperty(property.getKey()));
                        }
                    }
                }

                scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

//                // Execute any engine-specific configuration before the script is evaluated
//                ScriptEngineConfigurator configurator =
//                        scriptingComponentHelper.scriptEngineConfiguratorMap.get(scriptingComponentHelper.getScriptEngineName().toLowerCase());
//
//                // Evaluate the script with the configurator (if it exists) or the engine
//                if (configurator != null) {
//                    configurator.eval(scriptEngine, scriptToRun, scriptingComponentHelper.getModules());
//                } else {
                    scriptEngine.eval("print('Hello, World')");
//                }

                // Commit this session for the user. This plus the outermost catch statement mimics the behavior
                // of AbstractProcessor. This class doesn't extend AbstractProcessor in order to share a base
                // class with InvokeScriptedProcessor
                session.commit();
            } catch (ScriptException e) {
                throw new ProcessException(e);
            }
        } catch (final Throwable t) {
            // Mimic AbstractProcessor behavior here
            getLogger().error("{} failed to process due to {}; rolling back session", new Object[]{this, t});
            session.rollback(true);
            throw t;
//        } finally {
//            scriptingComponentHelper.engineQ.offer(scriptEngine);
        }
	}


//	@OnStopped
//    public void stop() {
//        scriptingComponentHelper.stop();
//    }
}
