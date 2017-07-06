Next Step After Generate Project

1. Check src/test/resources/application.properties
2. Check src/test/resources/applicationContext.xml and base package in the
   <context:component-scan base-package="${GROUP_ID}" /> 
   is the service package you want to be generated
3. Check src/test/resources/applicationContext-persistent.xml
   is the entity and repository package you want to be generated