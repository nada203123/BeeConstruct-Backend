FROM depot.dpc.com.tn:8443/tools/java-17-dpc-jre:latest

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/OffreService-0.0.1-SNAPSHOT.jar OffreBeeConstruct.jar

EXPOSE 8095

# Run the application
ENTRYPOINT ["java", "-jar", "OffreBeeConstruct.jar"]
