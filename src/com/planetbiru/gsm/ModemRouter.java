package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.List;

public class ModemRouter {
	private int currentIndex = -1;
	private List<Integer> modemIndex = new ArrayList<>();
	
	public ModemRouter(int index) {
		this.modemIndex.add(Integer.valueOf(index));
	}

	public ModemRouter() {
		/**
		 * Do nothing
		 */
	}

	public void addIndex(int index) {
		this.modemIndex.add(Integer.valueOf(index));
	}

	public int getIndex() throws ModemNotFoudException
	{
		if(this.modemIndex.isEmpty())
		{
			throw new ModemNotFoudException("Invalid GSM router");
		}
		this.currentIndex++;
		if(this.currentIndex >= this.modemIndex.size())
		{
			this.currentIndex = 0;
		}
		return this.modemIndex.get(this.currentIndex).intValue();
	}
}
