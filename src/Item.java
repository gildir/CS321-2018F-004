public class Item
{
	String name;
	double  weight;
	double price;

	public void Item(String name, double weight, double price)
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
}
