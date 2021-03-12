package course.spring;

public class MySoldierDI implements Soldier{
	WeaponDI weaponDI;
	
	//建構子允許武器參數以interface型態輸入
	public MySoldierDI(WeaponDI weaponDI) {
		this.weaponDI = weaponDI;
	}
	
	@Override
	public void destroyTarget() {
		this.weaponDI.attack();
		
	}

}
