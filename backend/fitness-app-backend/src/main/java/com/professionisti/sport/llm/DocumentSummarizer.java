package com.professionisti.sport.llm;

// import dev.langchain4j.service.UserMessage; // Would come from quarkus-langchain4j
// import dev.langchain4j.service.AiService; // Would come from quarkus-langchain4j

// @AiService // This annotation would activate it if LangChain4j is configured
public interface DocumentSummarizer {

    // @UserMessage
    String summarize(String documentText);

    // String summarize(String documentText, String socialMediaProfileText); // Example for combined summary
}
