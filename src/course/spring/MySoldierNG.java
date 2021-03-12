package course.spring;

public class MySoldierNG implements Soldier{
	Gun weapon;
	public MySoldierNG() {
		this.weapon = new Gun();
	}

	@Override
	public void destroyTarget() {
		this.weapon.attack();
		
	}

}
