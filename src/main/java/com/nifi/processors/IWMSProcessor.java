package com.nifi.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.AbstractSessionFactoryProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSessionFactory;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import com.sun.javafx.property.adapter.PropertyDescriptor;
import com.sun.xml.internal.bind.api.TypeReference;

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
//		ScriptEngineManager factory = new ScriptEngineManager();
//	    ScriptEngine scriptEngine = factory.getEngineByName("JavaScript");
////	    engine.eval("print('Hello, World')");
//	    
//		ProcessSession session = sessionFactory.createSession();
//        try {
//
//            try {
//                Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
//                if (bindings == null) {
//                    bindings = new SimpleBindings();
//                }
//                bindings.put("session", session);
//                bindings.put("context", context);
////                bindings.put("log", log);
//                bindings.put("REL_SUCCESS", REL_SUCCESS);
//                bindings.put("REL_FAILURE", REL_FAILURE);
//
//                // Find the user-added properties and set them on the script
//                for (Map.Entry<PropertyDescriptor, String> property : context.getProperties().entrySet()) {
//                    if (property.getKey().isDynamic()) {
//                        // Add the dynamic property bound to its full PropertyValue to the script engine
//                        if (property.getValue() != null) {
//                            bindings.put(property.getKey().getName(), context.getProperty(property.getKey()));
//                        }
//                    }
//                }
//
//                scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
//
////                // Execute any engine-specific configuration before the script is evaluated
////                ScriptEngineConfigurator configurator =
////                        scriptingComponentHelper.scriptEngineConfiguratorMap.get(scriptingComponentHelper.getScriptEngineName().toLowerCase());
////
////                // Evaluate the script with the configurator (if it exists) or the engine
////                if (configurator != null) {
////                    configurator.eval(scriptEngine, scriptToRun, scriptingComponentHelper.getModules());
////                } else {
//                    scriptEngine.eval("print('Hello, World')");
////                }
//
//                // Commit this session for the user. This plus the outermost catch statement mimics the behavior
//                // of AbstractProcessor. This class doesn't extend AbstractProcessor in order to share a base
//                // class with InvokeScriptedProcessor
//                session.commit();
//            } catch (ScriptException e) {
//                throw new ProcessException(e);
//            }
//        } catch (final Throwable t) {
//            // Mimic AbstractProcessor behavior here
//            getLogger().error("{} failed to process due to {}; rolling back session", new Object[]{this, t});
//            session.rollback(true);
//            throw t;
////        } finally {
////            scriptingComponentHelper.engineQ.offer(scriptEngine);
//        }
		try {
		HttpResponse<String> response = Unirest.post("http://34.213.227.157/InnoweraWMS/api/V2/Authorization/AuthorizationToken")
				  .header("apitoken", "17b849bca9714b628ed2d3a4d0efe129")
				  .header("iwmskey", "03f8faf5e29140fc859007d63e39ee00")
				  .header("content-type", "application/json")
				  .header("format", "application/json")
				  .asString();
		ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(response, new TypeReference<Map<String, Object>>() {
        });
        JsonNode data = mapper.readTree(map.get("Data"));
        String token = result.get("AuthorizationToken");
        
        try {
        	String logonId = "1";
        	String processCode = "MM02";
        	String processFileName = "MM02_Change_Material.itf";
        	String processType = "Transaction";
        	String headerItems = "[['A','B','C'],['100-100','350','300']]";
        	String loopData = "";
        	String isTestRun = "";
        	
        	ObjectNode jsonObject = mapper.createObjectNode();
        	jsonObject.put("LogonId",logonId);
        	jsonObject.put("ProcessCode",processCode);
        	jsonObject.put("ProcessFileName",processFileName);
        	jsonObject.put("ProcessType",processType);
        	jsonObject.put("HeaderItems",headerItems);
        	jsonObject.put("LoopData",loopData);
        	jsonObject.put("IsTestRun",isTestRun);
        	
        	String body = mapper.writeValueAsString(jsonObject);
        	
//        	{ LogonID: _LogonID, ProcessCode: _ProcessCode, ProcessFileName: _ProcessFileName, 
//        		ProcessType: _ProcessType, HeaderItems: _HeaderItems, LoopData: _FooterItems, IsTestRun: _IsTestRun }        	
        	HttpResponse<String> response = Unirest.post("http://34.213.227.157/InnoweraWMS/api/V2/ProcessManage/ExecuteProcessRequest")
  				.header("content-type", "application/json")
  				.header("format", "application/json")
  				.header("Authorization-Token", token)
  				  .body(body)
  				  .asString();
        }catch(Exception e) {
        	e.printStackTrace();
        }
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}


//	@OnStopped
//    public void stop() {
//        scriptingComponentHelper.stop();
//    }
}
