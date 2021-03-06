package shootinggame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

class game_Frame extends JFrame implements KeyListener, Runnable {

    int f_width;
    int f_height;

    int x, y;

    int[] cx = {0, 0, 0};
    int bx = 0; // 전체 배경 스크롤 용 변수

    boolean keyUp = false;
    boolean keyDown = false;
    boolean keyLeft = false;
    boolean keyRight = false;
    boolean keySpace = false;

    int cnt; // 각종 타이밍 조절을 위한 무한 루프 카운터 변수

    int player_Speed; // 유저의 캐릭터가 움직이는 속도를 조절
    int missile_Speed;
    int fire_Speed;
    int enemy_Speed;
    int player_Status = 0;
    // 유저 캐릭터 상태 체크 변수 0: 평상시, 1:미사일발사 2: 충돌
    int game_Score;
    int player_Hitpoint;

    //int e_w, e_h; // 적 이미지의 크기값을 받을 변수;
    //int m_w, m_h; // 미사일 이미지의 크기값을 받을 변수;

    Thread th;

    Toolkit tk = Toolkit.getDefaultToolkit();

    Image[] Player_img;
    Image BackGround_img;
    Image[] Cloud_img;
    Image[] Explo_img;


    Image Missile_img;
    Image Missile2_img;
    Image Enemy_img;

    ArrayList Missile_List = new ArrayList();
    ArrayList Enemy_List = new ArrayList();
    ArrayList Explosion_List = new ArrayList();

    Image buffImage; //더블 버퍼링용
    Graphics buffg; //더블 버퍼링용

    Missile ms; // 미사일 클래스 접근키
    Enemy en;

    Explosion ex; // 폭발 이벤트 접근 키

    game_Frame() {
        init();
        start();

        setTitle("슈팅 게임 만들기");
        setSize(f_width, f_height);

        Dimension screen = tk.getScreenSize();

        int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
        int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);

        setLocation(f_xpos, f_ypos);
        setResizable(false);
        setVisible(true);
    }


    public void init() {
        x = 100;
        y = 100;
        f_width = 1200;
        f_height = 600;


        Missile_img = new ImageIcon("C:\\Users\\cjy17\\Desktop\\Solution\\practice\\src\\game1\\Missile.png").getImage();
        Missile2_img = new ImageIcon("C:\\Users\\cjy17\\Desktop\\Solution\\practice\\src\\game1\\Missile2.png").getImage();
        Enemy_img = new ImageIcon("C:\\Users\\cjy17\\Desktop\\Solution\\practice\\src\\game1\\enemy.png").getImage();

        Player_img = new Image[5];
        for (int i = 0; i < Player_img.length; i++) {
            Player_img[i] = new ImageIcon("C:\\Users\\cjy17\\Desktop\\Solution\\practice\\src\\game1\\f15k_" + i + ".png").getImage();
        }

        BackGround_img = new ImageIcon("C:\\Users\\cjy17\\Desktop\\Solution\\practice\\src\\game1\\background.png").getImage();
        Cloud_img = new Image[3];
        for (int i = 0; i < Cloud_img.length; i++) {
            Cloud_img[i] = new ImageIcon("C:\\Users\\cjy17\\Desktop\\Solution\\practice\\src\\game1\\cloud_" + i + ".png").getImage();
        }
        Explo_img = new Image[3];
        for (int i = 0; i < Explo_img.length; i++) {
            Explo_img[i] = new ImageIcon("C:\\Users\\cjy17\\Desktop\\Solution\\practice\\src\\game1\\explo_" + i + ".png").getImage();
        }

        game_Score = 0;
        player_Hitpoint = 3;

        player_Speed = 5;
        missile_Speed = 11;
        fire_Speed = 15;
        enemy_Speed = 7;


    }

    private int ImageHeightValue(String file) {

        int x = 0;
        try {
            File f = new File(file);
            BufferedImage bi = ImageIO.read(f);

            x = bi.getWidth();
        } catch (Exception e) {
        }
        return x;
    }


    private int ImageWidthValue(String file) {
        int y = 0;
        try {
            File f = new File(file);
            BufferedImage bi = ImageIO.read(f);

            y = bi.getHeight();

        } catch (Exception e) {
        }
        return y;
    }


    public void start() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addKeyListener(this);
        th = new Thread(this);
        th.start();

    }

    public void run() {
        try {
            while (true) {
                KeyProcess();
                EnemyProcess();
                MissileProcess();

                ExplosionProcess();

                repaint();

                Thread.sleep(20);
                cnt++;
            }
        } catch (Exception e) {

        }
    }

    private void ExplosionProcess() {
        for (int i = 0; i < Explosion_List.size(); i++) {
            ex = (Explosion) Explosion_List.get(i);
            ex.effect();
        }
    }


    private void EnemyProcess() {
        for (int i = 0; i < Enemy_List.size(); i++) {
            en = (Enemy) (Enemy_List.get(i));

            en.move();
            if (en.x < -200) {
                Enemy_List.remove(i);
            }


            if (cnt % 50 == 0) {
                ms = new Missile(en.x, en.y + 25, 180, missile_Speed, 1);
                Missile_List.add(ms);
            }
            if (Crash(x, y, en.x, en.y, Player_img[0], Enemy_img)) {
                // 플레이어와적의충돌을판정하여
                // boolean값을 리턴받아true면 아래를실행합니다.
                player_Hitpoint--; // 플레이어체력을1깍습니다.
                Enemy_List.remove(i); // 적을제거합니다.
                game_Score += 10;
                // 제거된적으로게임스코어를10 증가시킵니다.
                ex = new Explosion(en.x + Enemy_img.getWidth(null) / 2, en.y + Enemy_img.getHeight(null) / 2, 0);
                // 적이위치해있는곳의중심좌표x,y 값과
                // 폭발설정을받은값( 0 또는1 )을받습니다.
                // 폭발설정값- 0 : 폭발, 1 : 단순피격
                Explosion_List.add(ex);
                // 제거된적위치에폭발이펙트를추가합니다.
                ex = new Explosion(x, y, 1);
                // 적이위치해있는곳의중심좌표x,y 값과
                // 폭발설정을받은값( 0 또는1 )을받습니다.
                // 폭발설정값- 0 : 폭발, 1 : 단순피격
                Explosion_List.add(ex);
                // 충돌시플레이어의위치에충돌용이펙트를추가.
            }

        }

        if (cnt % 200 == 0) {
            //루프카운트를이용한적등장타이밍조절
            en = new Enemy(f_width + 100, 100, enemy_Speed);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 200, enemy_Speed);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 300, enemy_Speed);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 400, enemy_Speed);
            Enemy_List.add(en);
            en = new Enemy(f_width + 100, 500, enemy_Speed);
            Enemy_List.add(en);

            // 적움직임속도를추가로받아적을생성한다.

        }
    }


    private void MissileProcess() {
        if (keySpace) {
            player_Status = 1;

            if ((cnt % fire_Speed) == 0) {
                ms = new Missile(x + 150, y + 30, 0, missile_Speed, 0);
                Missile_List.add(ms);

                ms = new Missile(x + 150, y + 30, 330, missile_Speed, 0);
                Missile_List.add(ms);

                ms = new Missile(x + 150, y + 30, 30, missile_Speed, 0);
                Missile_List.add(ms);
            }
        }

        for (int i = 0; i < Missile_List.size(); i++) {
            ms = (Missile) (Missile_List.get(i));
            ms.move();

            if (ms.x > f_width - 20 || ms.x < 0 || ms.y < 0 || ms.y > f_height) {
                Missile_List.remove(i);
            }

            if (Crash(x, y, ms.x, ms.y, Player_img[0], Missile_img) && ms.who == 1) {
                player_Hitpoint--;

                ex = new Explosion(x, y, 1);

                Explosion_List.add(ex);

                Missile_List.remove(i);
            }
            for (int j = 0; j < Enemy_List.size(); j++) {
                en = (Enemy) Enemy_List.get(j);

                if (Crash(ms.x, ms.y, en.x, en.y, Missile_img, Enemy_img) && ms.who == 0) {

                    Missile_List.remove(i);
                    Enemy_List.remove(j);
                    game_Score += 10;
                    ex = new Explosion(en.x + Enemy_img.getWidth(null) / 2, en.y + Enemy_img.getHeight(null) / 2, 0);
                    Explosion_List.add(ex);
                }

            }

        }

    }


    private boolean Crash(int x1, int y1, int x2, int y2, Image img1, Image img2) {
        boolean chk = false;

        if (Math.abs((x1 + img1.getWidth(null) / 2) - (x2 + img2.getWidth(null) / 2)) < (img2.getWidth(null) / 2 + img1.getWidth(null) / 2) &&
                Math.abs((y1 + img1.getHeight(null) / 2) - (y2 + img2.getHeight(null) / 2)) < (img2.getHeight(null) / 2 + img1.getHeight(null) / 2)) {
            chk = true;
        } else {
            chk = false;
        }
        return chk;

    }


    public void paint(Graphics g) {

        buffImage = createImage(f_width, f_height);
        //더블 버퍼링 버퍼 크기를 화면크기와 같게 설정
        buffg = buffImage.getGraphics();

        update(g);

    }

    public void update(Graphics g) {
        Draw_Background();
        Draw_Player();

        Draw_Missile();
        Draw_Enemy();

        Draw_Explosion();
        Draw_StatusText();

        g.drawImage(buffImage, 0, 0, this);
    }

    private void Draw_StatusText() {
        buffg.setFont(new Font("Default", Font.BOLD, 20));
        buffg.drawString("SCORE : " + game_Score, 1000, 70);
        buffg.drawString("HitPoint : " + player_Hitpoint, 1000, 90);
        buffg.drawString("Missile Count : " + Missile_List.size(), 1000, 110);
        buffg.drawString("Enemy Count : " + Enemy_List.size(), 1000, 130);

    }


    private void Draw_Explosion() {

        for (int i = 0; i < Explosion_List.size(); i++) {
            ex = (Explosion) Explosion_List.get(i);

            if (ex.damage == 0) {
                if (ex.ex_cnt < 7) {
                    buffg.drawImage(Explo_img[0], ex.x - Explo_img[0].getWidth(null) / 2, ex.y - Explo_img[0].getHeight(null) / 2, this);
                } else if (ex.ex_cnt < 14) {
                    buffg.drawImage(Explo_img[1], ex.x + 60, ex.y + 5, this);
                } else if (ex.ex_cnt < 21) {
                    buffg.drawImage(Explo_img[0], ex.x + 5, ex.y + 10, this);
                } else if (ex.ex_cnt > 21) {
                    Explosion_List.remove(i);
                    ex.ex_cnt = 0;
                }
            }
        }

    }


    private void Draw_Player() {
        switch (player_Status) {
            case 0:
                if ((cnt / 5 % 2) == 0) {
                    buffg.drawImage(Player_img[1], x, y, this);
                } else {
                    buffg.drawImage(Player_img[2], x, y, this);
                }
                break;
            case 1: // 미사일 발사시
                if ((cnt / 5 % 2 == 0)) {
                    buffg.drawImage(Player_img[3], x, y, this);
                } else {
                    buffg.drawImage(Player_img[4], x, y, this);
                }
                player_Status = 0;

                break;
            case 2:
                break;
        }


    }


    private void Draw_Background() {//배경이미지
        buffg.clearRect(0, 0, f_width, f_height);
        // 화면 지우기 명령
        if (bx > -3500) {
            buffg.drawImage(BackGround_img, bx, 0, this);
            bx -= 1;
        } else {
            bx = 0;
        }

        for (int i = 0; i < cx.length; i++) {
            if (cx[i] < 1400) {
                cx[i] += 5 + i * 3;
            } else {
                cx[i] = 0;
            }

            buffg.drawImage(Cloud_img[i], 1200 - cx[i], 50 + i * 200, this);
        }

    }


    private void Draw_Enemy() {
        for (int i = 0; i < Enemy_List.size(); i++) {
            en = (Enemy) (Enemy_List.get(i));
            buffg.drawImage(Enemy_img, en.x, en.y, this);
        }

    }


    private void Draw_Missile() {
        for (int i = 0; i < Missile_List.size(); i++) {

            ms = (Missile) (Missile_List.get(i));
            //미사일 위치 값을 확인

            if (ms.who == 0) buffg.drawImage(Missile_img, ms.x, ms.y, this);

            if (ms.who == 1) buffg.drawImage(Missile2_img, ms.x, ms.y, this);


        }
    }


    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:

                keyUp = true;
                break;
            case KeyEvent.VK_DOWN:
                keyDown = true;
                break;
            case KeyEvent.VK_LEFT:
                keyLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
                keyRight = true;
                break;
            case KeyEvent.VK_SPACE:
                keySpace = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                keyUp = false;
                break;
            case KeyEvent.VK_DOWN:
                keyDown = false;
                break;
            case KeyEvent.VK_LEFT:
                keyLeft = false;
                break;
            case KeyEvent.VK_RIGHT:
                keyRight = false;
                break;
            case KeyEvent.VK_SPACE:
                keySpace = false;
                break;

        }

    }

    public void KeyProcess() {

        if (keyUp == true) {
            if (y > 20) {
                y -= 5;
            }
            player_Status = 0;
        }
        if (keyDown == true) {
            if (y + Player_img[0].getHeight(null) < f_height) {
                y += 5;
            }
            player_Status = 0;
        }
        if (keyLeft == true) {
            if (x > 0) {
                x -= 5;
            }
            player_Status = 0;
        }
        if (keyRight == true) {
            if (x + Player_img[0].getWidth(null) < f_width) {
                x += 5;
            }
            player_Status = 0;
        }

    }

}
