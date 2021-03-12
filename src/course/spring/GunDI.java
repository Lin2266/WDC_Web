package course.spring;

public class GunDI implements WeaponDI{
	//槍實作武器，攻擊
	@Override
	public void attack() {
		System.out.println("Gun shoot!!");
		
	}
	
}
