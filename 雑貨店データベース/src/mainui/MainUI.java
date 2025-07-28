package mainui;

import javax.swing.*;
import khohang.KhoHangUI;
import donnhaphang.DonNhapHangUI;
import donxuathang.DonXuatUI;
import loinhuan.LoiNhuanUI;
import doitac.NhaCungCapKhachHangUI;
import sanpham.SanPhamUI;
import databaseconnector.DatabaseConnector;
import store.StoreUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Locale;
public class MainUI extends JFrame {
// màu sắc 
private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
private static final Color COLOR_PRIMARY_TEXT_WHITE = Color.WHITE;
private static final Color COLOR_OVERLAY = new Color(0, 0, 0, 140);
private static final Color COLOR_TEXT_SHADOW = new Color(0, 0, 0, 70);
private static final Color COLOR_BORDER = new Color(224, 224, 224);
private static final Color COLOR_HEADER_BG = Color.WHITE;
private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
private static final Color COLOR_BUTTON_BORDER = new Color(220, 220, 220); 

// Font 
private static final Font FONT_LOGO = new Font("Cambria", Font.BOLD, 26);
private static final Font FONT_CARD_TITLE = new Font("Cambria", Font.BOLD, 40);
private static final Font FONT_FOOTER = new Font("Cambria", Font.PLAIN, 14);
private static final Font FONT_KPI_TITLE = new Font("Cambria", Font.BOLD, 13);
private static final Font FONT_KPI_VALUE = new Font("Cambria", Font.BOLD, 20);
private static final Font FONT_ACTION_BUTTON = new Font("Cambria", Font.BOLD, 13); 


// KPI 
private JLabel kpiRevenueValueLabel;
private JLabel kpiLowStockValueLabel;
private JLabel kpiNewOrdersValueLabel;

public MainUI() {
    setTitle("MainMenu");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    initUI();
}

private void initUI() {
    Container contentPane = getContentPane();
    contentPane.setBackground(COLOR_BACKGROUND);
    contentPane.setLayout(new BorderLayout(0, 0));

    contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
    contentPane.add(createContentPanelWrapper(), BorderLayout.CENTER);
    contentPane.add(createFooterPanel(), BorderLayout.SOUTH);
}


private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout(50, 0));
    headerPanel.setBackground(COLOR_HEADER_BG);
    headerPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
        BorderFactory.createEmptyBorder(15, 50, 15, 50)
    ));

    JLabel logoLabel = new JLabel("HỆ THỐNG QUẢN LÝ");
    logoLabel.setFont(FONT_LOGO);
    logoLabel.setForeground(COLOR_HEADER_TEXT);
    headerPanel.add(logoLabel, BorderLayout.WEST);

    // KPI 
    JPanel kpiContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
    kpiContainer.setOpaque(false);
    
    kpiRevenueValueLabel = new JLabel("0 VNĐ");
    kpiLowStockValueLabel = new JLabel("0");
    kpiNewOrdersValueLabel = new JLabel("0");

    kpiContainer.add(createKpiBox("DOANH THU HÔM NAY", kpiRevenueValueLabel, "/image/dentaku_businessman.png"));
    kpiContainer.add(createKpiBox("HÀNG SẮP HẾT", kpiLowStockValueLabel, "/image/souko_building.png"));
    kpiContainer.add(createKpiBox("ĐƠN HÀNG MỚI", kpiNewOrdersValueLabel, "/image/trade_container_character_crane.png"));
    
    // 2. TẠO NÚT MỚI ĐỂ MỞ CỬA HÀNG
    JButton storeButton = new JButton("Cửa Hàng");
    URL storeIconUrl = getClass().getResource("/image/building_zakkaya.png"); 
    if (storeIconUrl != null) {
        ImageIcon storeIcon = new ImageIcon(storeIconUrl);
        Image scaledImage = storeIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        storeButton.setIcon(new ImageIcon(scaledImage));
    }
    styleActionButton(storeButton); 
    storeButton.addActionListener(e -> {
        new StoreUI(this).setVisible(true);
    });
    
    // 3. TẠO MỘT PANEL BÊN PHẢI ĐỂ GÓI KPI VÀ NÚT CỬA HÀNG LẠI
    JPanel rightPanel = new JPanel(new BorderLayout(25, 0)); 
    rightPanel.setOpaque(false);
    rightPanel.add(kpiContainer, BorderLayout.CENTER);
    rightPanel.add(storeButton, BorderLayout.EAST);
    
    // 4. THÊM rightPanel VÀO HEADER
    headerPanel.add(rightPanel, BorderLayout.EAST);
    
    refreshKpis();
    
    return headerPanel;
}

// <<<< THÊM PHƯƠNG THỨC MỚI ĐỂ STYLE CHO NÚT >>>>
private void styleActionButton(JButton button) {
    button.setFont(FONT_ACTION_BUTTON);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setFocusPainted(false);
    button.setOpaque(true);
    button.setBackground(COLOR_HEADER_BG);
    button.setForeground(COLOR_HEADER_TEXT);
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(COLOR_BUTTON_BORDER), 
        BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    
    button.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            button.setBackground(COLOR_BACKGROUND);
        }
        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(COLOR_HEADER_BG);
        }
    });
}

private JPanel createKpiBox(String title, JLabel valueLabel, String iconPath) {
    JPanel panel = new JPanel(new BorderLayout(15, 0));
    panel.setOpaque(false);

    try {
        URL url = getClass().getResource(iconPath);
        if(url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            panel.add(iconLabel, BorderLayout.WEST);
        }
    } catch (Exception e) {
        System.err.println("Không tìm thấy icon KPI: " + iconPath);
    }

    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.setOpaque(false);
    
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(FONT_KPI_TITLE);
    titleLabel.setForeground(COLOR_SECONDARY_TEXT);

    valueLabel.setFont(FONT_KPI_VALUE);
    valueLabel.setForeground(COLOR_HEADER_TEXT);
    
    textPanel.add(titleLabel);
    textPanel.add(valueLabel);
    
    panel.add(textPanel, BorderLayout.CENTER);

    return panel;
}

public void refreshKpis() {
    String sqlRevenue = "SELECT COALESCE(SUM(tong_tien), 0) FROM donxuat WHERE ngay_xuat = CURRENT_DATE;";
    String sqlLowStock = "SELECT COUNT(*) FROM sanpham WHERE so_luong < 10;";
    String sqlNewOrders = "SELECT COUNT(*) FROM donxuat WHERE ngay_xuat = CURRENT_DATE;";

    try (Connection conn = DatabaseConnector.getConnection();
         Statement stmt = conn.createStatement()) {
        
        ResultSet rs = stmt.executeQuery(sqlRevenue);
        if (rs.next()) {
            long revenue = rs.getLong(1);
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            kpiRevenueValueLabel.setText(currencyFormatter.format(revenue));
        }
        rs.close();
        
        rs = stmt.executeQuery(sqlLowStock);
        if(rs.next()) {
            kpiLowStockValueLabel.setText(String.valueOf(rs.getInt(1)));
        }
        rs.close();
        
        rs = stmt.executeQuery(sqlNewOrders);
        if(rs.next()) {
            kpiNewOrdersValueLabel.setText(String.valueOf(rs.getInt(1)));
        }
        rs.close();
        
    } catch (Exception e) {
        e.printStackTrace();
        kpiRevenueValueLabel.setText("Lỗi DB");
        kpiLowStockValueLabel.setText("Lỗi DB");
        kpiNewOrdersValueLabel.setText("Lỗi DB");
    }
}

private JScrollPane createContentPanelWrapper() {
    JPanel contentPanel = new JPanel(new GridLayout(2, 3, 0, 0));
    contentPanel.setBackground(COLOR_BACKGROUND);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    contentPanel.add(new HoverCardPanel("Quản lý Sản phẩm", "/image/baibai1.jpg", 
        (mainUI) -> new SanPhamUI(mainUI).setVisible(true)));
    
    contentPanel.add(new HoverCardPanel("Quản lý Kho hàng", "/image/baibai2.jpg", 
        (mainUI) -> new KhoHangUI(mainUI).setVisible(true)));
        
    contentPanel.add(new HoverCardPanel("ncc & khách hàng", "/image/baibai3.jpg", 
        (mainUI) -> new NhaCungCapKhachHangUI(mainUI).setVisible(true)));
        
    contentPanel.add(new HoverCardPanel("Nhập hàng", "/image/baibai4.jpg", 
        (mainUI) -> new DonNhapHangUI(mainUI).setVisible(true)));
        
    contentPanel.add(new HoverCardPanel("Xuất hàng", "/image/baibai5.jpg", 
        (mainUI) -> new DonXuatUI(mainUI).setVisible(true)));
        
    contentPanel.add(new HoverCardPanel("Quản lý lợi nhuận", "/image/baibai6.jpg", 
        (mainUI) -> new LoiNhuanUI(mainUI).setVisible(true)));

    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
}

private JPanel createFooterPanel() {
    JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
    footerPanel.setBackground(COLOR_HEADER_BG);
    footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
    JLabel footerLabel = new JLabel("© 2025 THCSDL - NHÓM 11.");
    footerLabel.setFont(FONT_FOOTER);
    footerLabel.setForeground(COLOR_SECONDARY_TEXT);
    footerPanel.add(footerLabel);
    return footerPanel;
}

class HoverCardPanel extends JPanel {
    private final BufferedImage image;
    private final String title;
    private final Consumer<MainUI> action; 
    private final Timer animationTimer;
    private float overlayAlpha = 0.0f;
    private int textYOffset = 50;
    private double scale = 1.0;
    private boolean isMouseInside = false;

    private static final float ALPHA_INCREMENT = 0.05f;
    private static final int Y_INCREMENT = 3;
    private static final double SCALE_INCREMENT = 0.005; 
    private static final double MAX_SCALE = 1.1;        
    private static final double MIN_SCALE = 1.0;

    public HoverCardPanel(String title, String imagePath, Consumer<MainUI> action) {
        this.title = title.toUpperCase();
        this.action = action;
        this.image = loadImage(imagePath);
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.setBorder(BorderFactory.createLineBorder(COLOR_BACKGROUND, 2));
        this.animationTimer = new Timer(10, e -> animate());

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { isMouseInside = true; animationTimer.start(); }
            @Override public void mouseExited(MouseEvent e) { isMouseInside = false; animationTimer.start(); }
            @Override public void mouseClicked(MouseEvent e) { 
                action.accept(MainUI.this); 
            }
        });
    }

    private void animate() {
        if (isMouseInside) {
            overlayAlpha = Math.min(1.0f, overlayAlpha + ALPHA_INCREMENT);
            textYOffset = Math.max(0, textYOffset - Y_INCREMENT);
            scale = Math.min(MAX_SCALE, scale + SCALE_INCREMENT);
            if (overlayAlpha >= 1.0f && textYOffset <= 0 && scale >= MAX_SCALE) { animationTimer.stop(); }
        } else {
            overlayAlpha = Math.max(0.0f, overlayAlpha - ALPHA_INCREMENT);
            textYOffset = Math.min(50, textYOffset + Y_INCREMENT);
            scale = Math.max(MIN_SCALE, scale - SCALE_INCREMENT);
            if (overlayAlpha <= 0.0f && textYOffset >= 50 && scale <= MIN_SCALE) { animationTimer.stop(); }
        }
        repaint();
    }

    private BufferedImage loadImage(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) throw new IOException("Resource not found: " + path);
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setClip(0, 0, getWidth(), getHeight());

        if (image != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int newWidth = (int) (panelWidth * scale);
            int newHeight = (int) (panelHeight * scale);
            int x = (panelWidth - newWidth) / 2;
            int y = (panelHeight - newHeight) / 2;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(image, x, y, newWidth, newHeight, this);
        }

        if (overlayAlpha > 0) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha));
            g2d.setColor(COLOR_OVERLAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fMetrics = g2d.getFontMetrics(FONT_CARD_TITLE);
        int titleWidth = fMetrics.stringWidth(title);
        int xTitle = (getWidth() - titleWidth) / 2;
        int yTitle = (getHeight() - fMetrics.getHeight()) / 2 + fMetrics.getAscent() + textYOffset;

        g2d.setFont(FONT_CARD_TITLE);
        g2d.setColor(COLOR_TEXT_SHADOW);
        g2d.drawString(title, xTitle + 2, yTitle + 2);
        g2d.setColor(COLOR_PRIMARY_TEXT_WHITE);
        g2d.drawString(title, xTitle, yTitle);

        g2d.dispose();
    }
}

public static void main(String[] args) {
    try {
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
    } catch (Exception ex) {
        System.err.println("Failed to initialize LaF.");
    }
    SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
}}