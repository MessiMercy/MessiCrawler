package com.model.inter;

import java.util.List;

import com.model.SimpleInterviewExperiences;

public interface ISimpleInterviewExperiencesOperation {
	public List<SimpleInterviewExperiences> selectInterviewByID(int id);

	public void addInterview(SimpleInterviewExperiences simpleInterviewExperiences);

	public void deleteSimpleInterviewExperiences(int id);

	public List<SimpleInterviewExperiences> selectCompanyInterviews(int companyId);

}
