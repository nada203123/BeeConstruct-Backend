FROM depot.dpc.com.tn:8443/tools/java-17-dpc-jre:latest

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/gateaway_service-0.0.1-SNAPSHOT.jar BeeConstructgateaway_service-0.0.1-SNAPSHOT.jar

EXPOSE 9448

# Run the application
ENTRYPOINT ["java", "-jar", "BeeConstructgateaway_service-0.0.1-SNAPSHOT.jar"]