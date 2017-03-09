package com.model.inter;

import com.model.SimplePositionInfo;

public interface ISimplePositionInfoOperation {
	public SimplePositionInfo selectPositionByID(int positionId);

	public void addPosition(SimplePositionInfo simplePositionInfo);

	public void deleteSimplePositionInfo(int positionId);

}
