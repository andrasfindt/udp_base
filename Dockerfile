FROM openjdk:8-jre
VOLUME ["/data"]
WORKDIR /data
#ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=10302,server=y,suspend=y"
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=10302,server=y,suspend=n"
ENTRYPOINT [ "java" ]
CMD ["-jar","udp_base_jar/udp_base.jar", "-s", "debug_properties/debug.properties"]
#CMD ["-jar","udp_base.jar", "-v", "-t", "100", "--thread-count", "16"]