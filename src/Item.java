<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonProperty;

public class Item
{
	String name;
	double  weight;
	double price;

	public Item(@JsonProperty("name") String name, @JsonProperty("weight") double weight, @JsonProperty("price") double price)
=======
public class Item
{
	String name;
	double  weight;
	double price;

	public Item(String name, double weight, double price)
>>>>>>> refs/remotes/base/dev
	{
		this.name = name;
		this.weight = weight;
		this.price = price;
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
	
	public String toString()
	{
		String ret = this.name + " (" + this.weight + ") ";
		return ret;
	}
}
