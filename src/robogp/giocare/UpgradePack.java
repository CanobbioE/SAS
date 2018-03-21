public class UpgradePack {
	public static final String CardNameDiturbatore = "diturbatore di frequenze";
	public static final String CardNameControllante = "raggio controllante";
	public static final String CardNameDisturbante = "raggio disturbante";
	public static final String CardNameRespingente = "raggio respingente";
	public static final String CardNameCopiaIncolla = "copia e incolla";
	public static final String CardNameAttrattore = "raggio attrattore";
	public static final String CardNameRandomSwitch = "random switch";
	public static final String CardNameDoppioLaser = "doppio laser";
	public static final String CardNameRetroLaser = "retro laser";
	public static final String CardNameSuperLaser = "super laser";
	public static final String CardNameAccelera = "acceleratore";
	public static final String CardNameGiroscopio = "giroscopio";
	public static final String CardNameDualCore = "dual core";
	public static final String CardNameRetro = "retromarcia";
	public static final String CardNameFreni  = "freni";
	public static final String CardNameScudp = "scudo";
	public static final String CardNameBomba = "bomba";
	public static final String CardNameShift = "shift";

	private char type;
	private String name;
	private int quantity;

	public Upgrade(String n, char t, int q) {
		this.name = n;
		this.type = t;
		this.quantity = q;
	}

	public char getType() {
		return this.type;
	}

	public String getName() {
		return this.getName();
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int n) {
		this.quantity = n;
	}

	public void useOne() {
		this.quantity = quantity-1;
	}

	public static ArrayList<UpgradePack> generateDeck() {
		ArrayList<UpgradePack> deck = new ArrayList<>();

		deck.add(new UpgradePack(UpgradePack.CardNameRandomSwitch, 'A', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameRandomSwitch, 'A', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameCopiaIncolla, 'A', 3));

		deck.add(new UpgradePack(UpgradePack.CardNameFreni, 'B', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameFreni, 'B', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameAccelera, 'B', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameRetro, 'B', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameShift, 'B', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameShift, 'B', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameDualCore, 'B', 3));

		deck.add(new UpgradePack(UpgradePack.CardNameDiturbatore, 'C', 1));
		deck.add(new UpgradePack(UpgradePack.CardNameDiturbatore, 'C', 1));
		deck.add(new UpgradePack(UpgradePack.CardNameGiroscopio, 'C', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameGiroscopio, 'C', 5));

		deck.add(new UpgradePack(UpgradePack.CardNameScudp, 'D', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameScudp, 'D', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameBomba, 'D', 1));
		deck.add(new UpgradePack(UpgradePack.CardNameBomba, 'D', 1));
		deck.add(new UpgradePack(UpgradePack.CardNameSuperLaser, 'D', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameDoppioLaser, 'D', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameRespingente, 'D', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameAttrattore, 'D', 5));
		deck.add(new UpgradePack(UpgradePack.CardNameControllante, 'D', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameDisturbante, 'D', 3));
		deck.add(new UpgradePack(UpgradePack.CardNameRetroLaser, 'D', 3));

		return deck;

	}
}