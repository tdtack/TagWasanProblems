package wasan.problems;

import java.util.ArrayList;

/**
 * ˜aZ}Œ`–â‘è‚ÉŠÜ‚Ü‚ê‚é‰~(Šô‰½—v‘fE}Œ`—v‘f)‚ÉŠÖ‚·‚éƒNƒ‰ƒX‚Å‚·B
 * 
 * @author Takuma Tsuchihashi
 *
 */
public class MyCircle {
	/** ‰~‚Ì’†S‚ğ•\‚·•Ï” */
	public MyPoint center;// ™

	/** ‰~‚Ì”¼Œa‚ğ•\‚·•Ï” */
	public double radius;// ™

	/** ‰~üã‚Ì8“_‚Ìî•ñ‚ğ‚Â”z—ñ */
	public MyPoint[] circum = new MyPoint[8];// ™

	/**
	 * ‰~‚Ì•û’ö®‚É‚¨‚¯‚é3‚Â‚ÌŒW”‚ğ•\‚·•Ï” <br>
	 * ‰~‚Ì•û’ö®‚Í x^2+bx+y^2+cy+d=0 ‚Å•\‚·‚à‚Ì‚Æ‚·‚éB
	 */
	public double b, c, d;// ™

	/** ã‹L‚Ì3‚Â‚ÌŒW”‚ğ³‹K‰»‚µ‚½•Ï” */
	public double nb, nc, nd;// ™

	/**
	 * ƒRƒ“ƒXƒgƒ‰ƒNƒ^
	 * 
	 * @param _center ‰~‚Ì’†S
	 * @param _radius ‰~‚Ì”¼Œa
	 */
	public MyCircle(MyPoint _center, double _radius) {// ™
		this.center = _center;
		this.radius = _radius;

		double twoPI = 360;
		double radian = twoPI / circum.length;
		for (int i = 0; i < circum.length; i++) {
			double x = this.center.x + this.radius * Math.cos(Math.toRadians(radian * i));
			double y = this.center.y + this.radius * Math.sin(Math.toRadians(radian * i));
			circum[i] = new MyPoint(x, y);
		}

		this.b = -2 * this.center.x;
		this.c = -2 * this.center.y;
		this.d = -this.circum[0].x * (this.b + this.circum[0].x) - this.circum[0].y * (this.c + this.circum[0].y);

		double scalar = Math.sqrt(this.b * this.b + this.c * this.c + this.d * this.d);
		this.nb = this.b / scalar;
		this.nc = this.c / scalar;
		this.nd = this.d / scalar;
	}

	/**
	 * ‰~‚©‚çŒ©‚½nŠpŒ`‚Æ‚ÌŠÖŒW« <br>
	 * ‡@nŠpŒ`‚©‚çŒ©‚½‰~‚Ì’†S‚Ì“àŠO <br>
	 * ‡AnŠpŒ`‚©‚çŒ©‚½‰~üã‚Ì8“_‚Ìó‘Ô(nŠpŒ`‚Ì“à‘¤, nŠpŒ`ã, nŠpŒ`‚ÌŠO‘¤) <br>
	 * ‡B‰~‚©‚çŒ©‚½nŠpŒ`‚Ì‘S‚Ä‚Ì•Ó‚Ìó‘Ô(‹——£‚ª”¼Œa–¢–, ‰~‚ÉÚ‚µ‚Ä‚¢‚é, ‹——£‚ª”¼Œa’´‰ß) <br>
	 */

	/**
	 * 
	 * @param pg
	 * @return
	 */
	public boolean inscribePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);// nŠpŒ`‚©‚çŒ©‚½‰~ã‚Ì8“_‚Ìó‘Ô
		int[] sideState = this.classifySide(pg.side);// ‰~‚©‚çŒ©‚½nŠpŒ`‚Ì•Ó‚Ìó‘Ô(‰~ü‚ÆnŠpŒ`‚Ì•Ó‚ÌÚó‘Ô)

		condition[0] = pg.includePoint(this.center);// ‡@“à‘¤‚É‚ ‚é
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length);// ‡A8ŒÂ‚Ì“_‚ªnŠpŒ`‚Ì“à‘¤‚Ü‚½‚ÍnŠpŒ`ã‚É‚ ‚é(‚»‚Ì‡Œv‚ª8)
		condition[2] = (sideState[1] == pg.side.size());// ‡Bn–{‚Ì•Ó‚ª‰~‚ÉÚ‚µ‚Ä‚¢‚é(‚»‚Ì‡Œv‚ªn)

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * ‡@“à‘¤‚É‚ ‚é <br>
	 * ‡A8ŒÂ‚Ì“_‚ªnŠpŒ`‚Ì“à‘¤‚Ü‚½‚ÍnŠpŒ`ã‚É‚ ‚é(‚»‚Ì‡Œv‚ª8) <br>
	 * ‡Bn–{–¢–‚Ì•Ó‚ª‰~‚ÉÚ‚µ‚Ä‚¢‚é, 1–{ˆÈã‚Ì•Ó‚ª‹——£‚ª”¼Œa’´‰ßA“à‘¤‚É‚ ‚é‚Ì‚Í0–{(‚»‚Ì‡Œv‚ªn) <br>
	 * (‡B‚É‚Â‚¢‚Ä‚Í‘S‚Ä‚Ì•Ó‚ğ‹á–¡‚µ‚È‚¯‚ê‚Î‚¢‚¯‚È‚¢‚©?A‡Œv‚ªn‚É‚È‚é‚æ‚¤‚É)
	 * 
	 * @param pg
	 * @return
	 */
	public boolean insidePolygon(MyPolygon pg) {
		boolean[] condition = new boolean[3];

		int[] circumState = pg.classifyCircum(this.circum);// nŠpŒ`‚©‚çŒ©‚½‰~ã‚Ì8“_‚Ìó‘Ô
		int[] sideState = this.classifySide(pg.side);// ‰~‚©‚çŒ©‚½nŠpŒ`‚Ì•Ó‚Ìó‘Ô(‰~ü‚ÆnŠpŒ`‚Ì•Ó‚ÌÚó‘Ô)

		condition[0] = pg.includePoint(this.center);// nŠpŒ`‚ª‰~‚Ì’†S‚ğŠÜ‚Ş‚©
		condition[1] = ((circumState[0] + circumState[1]) == this.circum.length); // 8“_‚É‚Â‚¢‚ÄAnŠpŒ`‚Ì“à‘¤or‰~ã
		condition[2] = (sideState[0] == 0) && withinRange(sideState[1], 0, pg.side.size() - 1)
				&& withinRange(sideState[2], 1, pg.side.size());// ™
		// ‰~‚Ì“à‘¤‚É‚ ‚é‚Ì‚Í0–{A‰~‚ÉÚ‚µ‚Ä‚¢‚é‚Ì‚Ín–{–¢–A1–{ˆÈã‚ªŠO‘¤‚É‚ ‚é

		return (condition[0] && condition[1] && condition[2]);
	}

	/**
	 * ‡@“àŠO‚Ç‚¿‚ç‚Ì‰Â”\«‚à‚ ‚é <br>
	 * ‡A1ŒÂˆÈã‚Ì“_‚ªnŠpŒ`‚Ì“à‘¤‚É‚ ‚éorÚ‚·‚é, 8ŒÂ–¢–(1ŒÂˆÈã)‚Ì“_‚ªnŠpŒ`‚ÌŠO‘¤‚É‚ ‚é(‚»‚Ì‡Œv‚ª8) <br>
	 * ‡B1–{ˆÈã‚Ì•Ó‚ª‹——£‚ª”¼Œa–¢–, n–{–¢–‚Ì•Ó‚ª‰~‚ÉÚ‚µ‚Ä‚¢‚é‚Ü‚½‚Í‹——£‚ª”¼Œa’´‰ß(‚»‚Ì‡Œv‚ªn) <br>
	 * 
	 * @param pg
	 * @return
	 */
	public boolean overlapPolygon(MyPolygon pg) {
		boolean[][] condition = new boolean[3][2];

		int[] circumState = pg.classifyCircum(this.circum);// nŠpŒ`‚©‚çŒ©‚½‰~ã‚Ì8“_‚Ìó‘Ô
		int[] sideState = this.classifySide(pg.side);// ‰~‚©‚çŒ©‚½nŠpŒ`‚Ì•Ó‚Ìó‘Ô(‰~ü‚ÆnŠpŒ`‚Ì•Ó‚ÌÚó‘Ô)

		condition[0][0] = true;// ‚Ç‚¿‚ç‚Ì‰Â”\«‚à‚ ‚é‚Ì‚Åtrue‚É‚·‚é
		condition[1][0] = withinRange((circumState[0] + circumState[1]), 1, this.circum.length - 1)
				&& withinRange(circumState[2], 1, this.circum.length - 1);

		condition[2][0] = withinRange(sideState[0], 1, pg.side.size() - 1)
				&& withinRange((sideState[1] + sideState[2]), 1, pg.side.size() - 1);
		condition[2][1] = (sideState[0] == pg.side.size()) && (this.classifyVertex(pg.vertex)[2] > 0);// “Áê‚Èó‹µ‚Ìê‡(nŠpŒ`‚ª‰~‚É“àÚ‚·‚é‚Æ‹æ•Ê)
		// (sideState[0] == pg.side.size())‚Ì‚İAŒë”»’è‚ğœ‚­‚½‚ß‚É•Ê‚ÌğŒ‚ğ•t—^‚·‚é

		return (condition[0][0] && condition[1][0] && (condition[2][0] || condition[2][1]));
	}

	/**
	 * 2‚Â‚Ì‰~“¯m‚ÌŠÖŒW« <br>
	 * ‡@‰~B‚©‚çŒ©‚½‰~A‚Ì’†S‚Ì“àŠO + ‰~A‚©‚çŒ©‚½‰~B‚Ì’†S‚Ì“àŠO <br>
	 * ‡A2‰~‚Ì’†SŠÔ‚Ì‹——£‚ÅŠY“–‚·‚éğŒ <br>
	 */

	/**
	 * ‡@“à‘¤‚É‚ ‚é + “àŠO‚Ç‚¿‚ç‚Ì‰Â”\«‚à‚ ‚é <br>
	 * ‡A’†SŠÔ‚Ì‹——£=‰~B‚Ì”¼Œa-‰~A‚Ì”¼Œa(â‘Î’l‚Íl—¶‚µ‚È‚¢) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean inscribeCircle(MyCircle c) {// ca.inscribe(cb) ca‚ªcb‚Ì’†
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;// â‘Î’l‚ğl—¶‚µ‚È‚¢

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isEqual(centerDist, radiusDiff));// Math.abs(centerDist - radiusDiff) < 10
		// c.radius - this.radius >= 0 ¨ false‚È‚ç‚Îreturn false;

		return (condition[0] && condition[1]);
	}

	/**
	 * ‡@“à‘¤‚É‚ ‚é + “àŠO‚Ç‚¿‚ç‚Ì‰Â”\«‚à‚ ‚é <br>
	 * ‡A’†SŠÔ‚Ì‹——£<‰~B‚Ì”¼Œa-‰~A‚Ì”¼Œa(â‘Î’l‚Íl—¶‚µ‚È‚¢) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean insideCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = c.radius - this.radius;// â‘Î’l‚ğl—¶‚µ‚È‚¢

		condition[0] = c.includePoint(this.center);
		condition[1] = (radiusDiff >= 0) && (isGreater(radiusDiff, centerDist));// ud‚È‚è‡‚¤v‚Æ”í‚ç‚È‚¢‚½‚ß‚ÌğŒ
		// condition[1] = (radiusDiff >= 0) && (Math.abs(centerDist - radiusDiff) >= 10)
		// && (centerDist < radiusDiff);// ud‚È‚è‡‚¤v‚Æ”í‚ç‚È‚¢‚½‚ß‚ÌğŒ

		return (condition[0] && condition[1]);
	}

	/**
	 * ‡@ŠO‘¤‚É‚ ‚é + ŠO‘¤‚É‚ ‚é <br>
	 * ‡A’†SŠÔ‚Ì‹——£=‰~A‚Ì”¼Œa+‰~B‚Ì”¼Œa <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean adjoinCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusSum = c.radius + this.radius;

		condition[0] = !c.includePoint(this.center) && !this.includePoint(c.center);
		condition[1] = isEqual(centerDist, radiusSum);// Math.abs(centerDist - radiusSum) < 10

		return (condition[0] && condition[1]);
	}

	/**
	 * ‡@“àŠO‚Ç‚¿‚ç‚Ì‰Â”\«‚à‚ ‚é + “àŠO‚Ç‚¿‚ç‚Ì‰Â”\«‚à‚ ‚é <br>
	 * ‡A‰~B‚Ì”¼Œa-‰~A‚Ì”¼Œa<’†SŠÔ‚Ì‹——£<‰~A‚Ì”¼Œa+‰~B‚Ì”¼Œa(â‘Î’l‚Íl—¶‚µ‚È‚¢) <br>
	 * 
	 * @param c
	 * @return
	 */
	public boolean overlapCircle(MyCircle c) {
		boolean[] condition = new boolean[2];

		double centerDist = c.center.calcDistToPoint(this.center);
		double radiusDiff = Math.abs(c.radius - this.radius);
		double radiusSum = c.radius + this.radius;

		condition[0] = true;
		condition[1] = (isGreater(centerDist, radiusDiff) && isGreater(radiusSum, centerDist));
		// (centerDist - radiusDiff) >= 10) && ((radiusSum - centerDist) >= 10

		return (condition[0] && condition[1]);
	}

	/////////////////////////////////////////////////////////////

	/**
	 * ‰~‚©‚çŒ©‚½nŠpŒ`‘S‚Ä‚Ì’¸“_‚Ì“àŠO
	 * 
	 * @param pg
	 * @return
	 */
	public int[] classifyVertex(ArrayList<MyPoint> vertex) {
		int[] vertexState = new int[3];// 0:‰~‚Ì“à‘¤, 1:‰~ã, 2:‰~‚ÌŠO‘¤

		for (int i = 0; i < vertex.size(); i++) {
			MyPoint p = vertex.get(i);
			double dist = this.center.calcDistToPoint(p);

			if (isEqual(this.radius, dist)) {// Math.abs(this.radius - dist) < 10
				vertexState[1]++;
			} else {
				if (this.radius > dist) {
					vertexState[0]++;
				} else {
					vertexState[2]++;
				}
			}
		}
		return vertexState;
	}

	/**
	 * ‰~‚©‚çŒ©‚½nŠpŒ`‘S‚Ä‚Ì•Ó‚Ì“àŠO
	 * 
	 * @param pg
	 * @return
	 */
	public int[] classifySide(ArrayList<MyLine> side) {
		int[] sideState = new int[3];// 0:‰~‚Ì“à‘¤, 1:‰~‚ÉÚ‚·‚é, 2:‰~‚ÌŠO‘¤

		for (int i = 0; i < side.size(); i++) {// a•Ó‚ªû‚Ü‚Á‚Ä‚¢‚é‚©‚Ç‚¤‚©‚Í(¡‚Í)l—¶‚µ‚È‚¢‚à‚Ì‚Æ‚·‚é
			MyLine l = side.get(i);// aü•ª‚Å‚Í‚È‚­’¼ü‚Å‚ ‚é‚±‚Æ‚É’ˆÓ
			double dist = this.calcDistToLine(l); // ‚±‚¢‚Â‚ğ•ÏX‚·‚é?

			if (isEqual(this.radius, dist)) {// Math.abs(this.radius - dist) < 10
				sideState[1]++;// aÚ‚µ‚Ä‚¢‚é‚Ì‚¾‚¯l‚¦‚ê‚Î‚¢‚¢‚©
			} else {
				if (this.radius > dist) {
					sideState[0]++;
				} else {
					sideState[2]++;
				}
			}
		}

		return sideState;
	}

	/**
	 * ‰~‚©‚çŒ©‚½“_‚Ì“àŠO
	 * 
	 * @param p
	 * @return
	 */
	public boolean includePoint(MyPoint p) {
		double dist = this.center.calcDistToPoint(p);

		return isGreater(this.radius, dist);
	}

	/**
	 * ‰~ü‚Æ“_‚Æ‚Ì‹——£
	 * 
	 * @param p
	 * @return
	 */
	double calcDistToPoint(MyPoint p) {
		return Math.abs(Math.hypot((p.x - center.x), (p.y - center.y)) - radius);
	}

	/**
	 * ‰~‚Ì’†S‚Æ’¼ü‚Ì‹——£
	 * 
	 * @param l
	 * @return
	 */
	double calcDistToLine(MyLine l) {
		MyPoint p = this.center.getPerpendicularFoot(l);

		if (p.withinLineRange(l)) {// ‰º‚ë‚µ‚½‚ü‚ªü•ª‚ÆŒğ‚í‚éê‡
			double numerator = Math.abs(l.a * this.center.x + l.b * this.center.y + l.c);
			double denominator = Math.hypot(l.a, l.b);

			return numerator / denominator;
		} else {
			if (isEqual(this.center.calcDistToPoint(l.start), 0)) {
				return 0;
			} else if (isEqual(this.center.calcDistToPoint(l.end), 0)) {
				return 0;
			}
		}
		return Double.MAX_VALUE;
	}

	private boolean withinRange(int value, int min, int max) {
		return (min <= value) && (value <= max);
	}

	private boolean isEqual(double a, double b) {// a‚Æb‚ª“™‚µ‚¢
		return Math.abs(a - b) < 10;// ™™™threshold
	}

	private boolean isGreater(double a, double b) {// a‚ªb‚æ‚è‘å‚«‚¢
		return (a - b) >= 10;// ™™™threshold
	}
}

