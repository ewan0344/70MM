import java.util.Scanner;

public class test2
{
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter a number:");
        String s = sc.nextLine();

        try
        {
            int i = Integer.parseInt(s);
            System.out.println(i);
        }
        catch (NumberFormatException e)
        {
            System.out.println("Error: This is not a number!");
        }
    }
}