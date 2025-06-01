package com.fitconnect.llm;

import dev.langchain4j.service.AiService;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@AiService
public interface ProfessionalProfileAnalyzer {

    @SystemMessage("""
        You are an expert HR analyst specializing in the fitness, wellness, and sports industry.
        Your task is to analyze the provided professional profile information and document text.
        Focus on identifying concrete skills, specializations, years of experience in specific areas,
        and overall expertise.
        """)
    @UserMessage("""
        Analyze the following professional information:
        Profile Data:
        ---
        {profileData}
        ---
        Uploaded Document Texts (concatenated):
        ---
        {documentTexts}
        ---
        Based on all the provided text, please generate a concise summary of the professional's expertise,
        key skills, and specializations. The summary should be suitable for a quick overview by a potential client
        or an administrator. Maximum 200 words.
        """)
    String summarizeExpertiseAndSkills(String profileData, String documentTexts);

    @SystemMessage("""
        You are an expert HR analyst. Your task is to extract a list of key skills and specializations
        from the provided text. Present these as a comma-separated list.
        For example: Yoga Instruction, Strength Training, Nutrition Planning, Injury Rehabilitation.
        Only list distinct skills.
        """)
    @UserMessage("""
        Professional Profile Data:
        ---
        {profileData}
        ---
        Uploaded Document Texts (concatenated):
        ---
        {documentTexts}
        ---
        Extract the key skills and specializations as a comma-separated list.
        """)
    String extractSkillsList(String profileData, String documentTexts);
}
