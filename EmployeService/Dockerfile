FROM depot.dpc.com.tn:8443/tools/java-17-dpc-jre:latest

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/EmployeService-0.0.1-SNAPSHOT.jar EmployeService-0.0.1-SNAPSHOT.jar

EXPOSE 8093

# Run the application
ENTRYPOINT ["java", "-jar", "EmployeService-0.0.1-SNAPSHOT.jar"]
