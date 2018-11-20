import com.fasterxml.jackson.annotation.JsonProperty;

public class Item
{
	String name;
	double  weight;
	double price;
	String discrip;
	String flavor;

	public Item(@JsonProperty("name") String name, @JsonProperty("weight") double weight, @JsonProperty("price") double price, @JsonProperty("discrip") String discrip, @JsonProperty("flavor") String flavor)
	{
		this.name = name;
		this.weight = weight;
		this.price = price;
		this.discrip = discrip;
		this.flavor = flavor;
		
	}

	public void changeName(String name)
	{
		this.name = name;
	}

	public void changeWeight(double weight)
	{
		this.weight = weight;
	}

	public void changePrice(double price)
	{
		this.price = price;
	}

	public String getName()
	{
		return this.name;
	}

	public double getWeight()
	{
		return this.weight;
	}

	public double getPrice()
	{
		return this.price;
	}
	
	public void setDiscrip(String discrip)
	{
		this.discrip = discrip;
	}

	public void setFlavor(String flavor)
	{
		this.flavor = flavor;
	}

	public String getDiscrip()
	{
		return this.discrip;
	}

	public String getFlavor()
	{
		return this.flavor;	
	}
	
	public String toString()
	{
		String ret = this.name + " (" + this.weight + ") ";
		return ret;
	}
}
