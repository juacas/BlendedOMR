package org.uva.itast.blended.omr.scanners;

public class HoughResult
{

	public int	value;
	public int	rho;
	public double	theta;
	public double	degrees;

	public HoughResult(int value, int rho, double theta)
	{
		this.value=value;
		this.rho=rho;
		this.theta=theta;
		this.degrees=theta*180/Math.PI;
	}

}
