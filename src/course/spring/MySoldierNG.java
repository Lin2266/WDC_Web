package course.spring;

public class MySoldierNG implements Soldier{
	WeaponDI weapon;
	Alert alert;
	public MySoldierNG(WeaponDI w,Alert a) {
		this.weapon = w;
		this.alert = a;
	}

	@Override
	public void destroyTarget() {
		this.alert.beforeAttack();
		this.weapon.attack();
		this.alert.afterAttack();
		
	}

}
