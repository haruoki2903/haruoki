package store;

import databaseconnector.DatabaseConnector;
import mainui.MainUI;
import store.model.Cart;
import store.model.SanPham;
import store.view.CartDialog;
import store.view.ProductCardPanel;
import store.view.WrapLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StoreUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    // --- CÁC HẰNG SỐ STYLE ĐÃ ĐƯỢC CẬP NHẬT ---
    private static final String FONT_FAMILY = "Segoe UI"; // <<<< FONT CHỮ MỚI
    private static final Color MUJI_RED = new Color(128, 0, 0);
    private static final Color BACKGROUND_COLOR = new Color(248, 248, 248);
    private static final Color TEXT_COLOR_DARK = new Color(34, 34, 34);
    private static final Font FONT_BRAND = new Font(FONT_FAMILY, Font.BOLD, 28);
    private static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, 24);
    private static final Font FONT_MENU = new Font(FONT_FAMILY, Font.BOLD, 15);

    private final Cart cart = new Cart();
    private final MainUI mainUiRef;
    private JLabel cartCountLabel;
    private JPanel mainContentPanel;

    public StoreUI(MainUI mainUi) {
        this.mainUiRef = mainUi;
        setTitle("Cửa Hàng Bán Lẻ HARUOKI");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        initUI();
        showHomePage();
    }

    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setBackground(BACKGROUND_COLOR);
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
        
        JPanel viewportView = new JPanel(new BorderLayout());
        mainContentPanel = new JPanel();
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        viewportView.add(mainContentPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(viewportView);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(createMenuBar(), BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPane.add(centerPanel, BorderLayout.CENTER);
    }
    
    // <<<< TÁI CẤU TRÚC HEADER ĐỂ GOM CÁC THÀNH PHẦN >>>>
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)),
            new EmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("HARUOKI");
        titleLabel.setFont(FONT_BRAND);
        titleLabel.setForeground(TEXT_COLOR_DARK);

        // --- Panel bên phải chứa tìm kiếm và giỏ hàng ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setOpaque(false);
        
        // --- Thanh tìm kiếm ---
        JTextField searchField = new JTextField("Bạn đang muốn tìm kiếm gì?");
        searchField.setFont(new Font(FONT_FAMILY, Font.ITALIC, 14));
        searchField.setForeground(Color.GRAY);
        searchField.setPreferredSize(new Dimension(300, 40)); // Tăng chiều cao
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (searchField.getText().equals("Bạn đang muốn tìm kiếm gì?")) { searchField.setText(""); searchField.setFont(new Font(FONT_FAMILY, Font.PLAIN, 14)); searchField.setForeground(Color.BLACK); } }
            public void focusLost(FocusEvent e) { if (searchField.getText().isEmpty()) { searchField.setText("Bạn đang muốn tìm kiếm gì?"); searchField.setFont(new Font(FONT_FAMILY, Font.ITALIC, 14)); searchField.setForeground(Color.GRAY); } }
        });
        searchField.addActionListener(e -> searchProducts(searchField.getText()));
        
        // --- Giỏ hàng ---
        JButton cartButton = new JButton();
        cartButton.setToolTipText("Giỏ hàng");
        cartButton.setBackground(Color.WHITE);
        cartButton.setFocusPainted(false);
        cartButton.setBorderPainted(false);
        cartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        URL cartIconUrl = getClass().getResource("/image/takuhai_daibiki.png"); 
        if (cartIconUrl != null) {
            // <<<< TĂNG KÍCH THƯỚC ICON >>>>
            cartButton.setIcon(new ImageIcon(new ImageIcon(cartIconUrl).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        } else {
             cartButton.setText("Giỏ");
        }
        
        cartCountLabel = new JLabel("(0)");
        cartCountLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        cartCountLabel.setForeground(Color.DARK_GRAY);
        cartCountLabel.setBorder(new EmptyBorder(0, 2, 0, 0));
        
        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cartPanel.setOpaque(false);
        cartPanel.add(cartButton);
        cartPanel.add(cartCountLabel);

        cartButton.addActionListener(e -> {
            new CartDialog(this, cart, mainUiRef).setVisible(true);
            updateCartCount();
        });

        rightPanel.add(searchField);
        rightPanel.add(cartPanel);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(MUJI_RED);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        menuBar.setOpaque(true);
        menuBar.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0)); // Đặt các nút menu
        menuBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JButton homeButton = new JButton("Trang Chủ");
        styleMenuButton(homeButton);
        homeButton.addActionListener(e -> showHomePage());
        menuBar.add(homeButton);

        List<String> khoHangList = getKhoHangNamesFromDB();
        for (String tenKho : khoHangList) {
            JButton menuButton = new JButton(tenKho);
            styleMenuButton(menuButton);
            menuButton.addActionListener(e -> showKhoHangProducts(tenKho));
            menuBar.add(menuButton);
        }

        JButton stockViewButton = new JButton("Quản Lý Kho");
        styleMenuButton(stockViewButton);
        stockViewButton.addActionListener(e -> showStockTableView());
        menuBar.add(stockViewButton);
        
        return menuBar;
    }

    private void styleMenuButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFont(FONT_MENU);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // Phương thức createProductSection không thay đổi, giữ nguyên code cũ
    private JPanel createProductSection(String title, List<SanPham> products) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBackground(BACKGROUND_COLOR);
        sectionPanel.setBorder(new EmptyBorder(40, 40, 30, 40));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SECTION_TITLE);
        titleLabel.setForeground(TEXT_COLOR_DARK);
        sectionPanel.add(titleLabel, BorderLayout.NORTH);

        if (products.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có sản phẩm nào phù hợp.");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBorder(new EmptyBorder(50, 0, 0, 0));
            sectionPanel.add(emptyLabel, BorderLayout.CENTER);
            return sectionPanel;
        }

        JPanel productRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        productRowPanel.setBackground(BACKGROUND_COLOR);
        for (SanPham sp : products) {
            ProductCardPanel card = new ProductCardPanel(sp, this);
            productRowPanel.add(card);
        }
        
        JScrollPane productScrollPane = new JScrollPane(productRowPanel);
        productScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        productScrollPane.setBorder(null);
        productScrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        productScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        sectionPanel.add(productScrollPane, BorderLayout.CENTER);
        return sectionPanel;
    }
    
    private void showHomePage() {
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));

        mainContentPanel.add(createProductSection("Sản phẩm mới", getNewestProductsFromDB(5)));
        mainContentPanel.add(createProductSection("Sản phẩm bán chạy", getTopSellingProductsFromDB(5)));

        List<String> khoNames = getKhoHangNamesFromDB();
        int khoToShow = Math.min(2, khoNames.size());
        for (int i = 0; i < khoToShow; i++) {
            String tenKho = khoNames.get(i);
            mainContentPanel.add(createProductSection("Từ " + tenKho, getProductsByKhoName(tenKho, 5)));
        }
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    // Phương thức showKhoHangProducts, searchProducts, showStockTableView không đổi
    // ...
    private void showKhoHangProducts(String tenKho) {
        List<SanPham> products = getProductsByKhoName(tenKho, 0); 
        
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout()); 
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.setBorder(new EmptyBorder(20, 40, 0, 40));
        JLabel titleLabel = new JLabel("Sản phẩm trong kho: " + tenKho);
        titleLabel.setFont(FONT_SECTION_TITLE);
        titleLabel.setForeground(TEXT_COLOR_DARK);
        titlePanel.add(titleLabel);
        
        JPanel productGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 25, 25));
        productGridPanel.setBackground(BACKGROUND_COLOR);
        productGridPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        if (products.isEmpty()) {
            productGridPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            productGridPanel.add(new JLabel("Kho này chưa có sản phẩm nào."));
        } else {
            for (SanPham sp : products) {
                productGridPanel.add(new ProductCardPanel(sp, this));
            }
        }
        
        mainContentPanel.add(titlePanel, BorderLayout.NORTH);
        mainContentPanel.add(productGridPanel, BorderLayout.CENTER);
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty() || searchTerm.equals("Bạn đang muốn tìm kiếm gì?")) return;
        
        List<SanPham> products = getProductsBySearchTerm(searchTerm.trim());

        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout()); 
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.setBorder(new EmptyBorder(20, 40, 0, 40));
        JLabel titleLabel = new JLabel("Kết quả tìm kiếm cho '" + searchTerm + "'");
        titleLabel.setFont(FONT_SECTION_TITLE);
        titleLabel.setForeground(TEXT_COLOR_DARK);
        titlePanel.add(titleLabel);
        
        JPanel productGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 25, 25));
        productGridPanel.setBackground(BACKGROUND_COLOR);
        productGridPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        if (products.isEmpty()) {
            productGridPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            productGridPanel.add(new JLabel("Không tìm thấy sản phẩm nào phù hợp."));
        } else {
            for (SanPham sp : products) {
                productGridPanel.add(new ProductCardPanel(sp, this));
            }
        }
        
        mainContentPanel.add(titlePanel, BorderLayout.NORTH);
        mainContentPanel.add(productGridPanel, BorderLayout.CENTER);
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void showStockTableView() {
        mainContentPanel.removeAll();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Bảng Tồn Kho Sản Phẩm");
        titleLabel.setFont(FONT_SECTION_TITLE);
        titleLabel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        String[] columnNames = {"Mã SP", "Tên Sản Phẩm", "Kho Hàng", "Nhà Cung Cấp", "Số Lượng Tồn"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable stockTable = new JTable(tableModel);
        stockTable.setFont(new Font("Arial", Font.PLAIN, 15));
        stockTable.setRowHeight(35);
        stockTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        String sql = "SELECT sp.ma_san_pham, sp.ten, kh.ten AS ten_kho, ncc.ten AS ten_ncc, sp.so_luong " +
                     "FROM sanpham sp " +
                     "LEFT JOIN nhacungcap ncc ON sp.ma_ncc = ncc.ma_ncc " +
                     "LEFT JOIN khohang kh ON sp.ma_kho = kh.ma_kho " +
                     "ORDER BY kh.ten, sp.ten";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("ma_san_pham"), rs.getString("ten"),
                    rs.getString("ten_kho") == null ? "N/A" : rs.getString("ten_kho"),
                    rs.getString("ten_ncc") == null ? "N/A" : rs.getString("ten_ncc"),
                    rs.getInt("so_luong")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }

        JScrollPane tableScrollPane = new JScrollPane(stockTable);
        tableScrollPane.setBorder(new EmptyBorder(0, 40, 40, 40));
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        mainContentPanel.add(titleLabel, BorderLayout.NORTH);
        mainContentPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // <<<< PHƯƠNG THỨC createHeaderPanel ĐÃ ĐƯỢC CẬP NHẬT ICON >>>>

    @Override
    public void actionPerformed(ActionEvent e) {
        String maSP = e.getActionCommand();
        SanPham sp = getProductByIdFromDB(maSP);
        
        if (sp != null) {
            if (sp.getSoLuongTon() > 0) {
                cart.themSanPham(sp.getMaSP(), sp.getTen(), sp.getDonGia(), 1);
                updateCartCount();
            } else {
                JOptionPane.showMessageDialog(this, "Sản phẩm đã hết hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void updateCartCount() {
        cartCountLabel.setText("(" + cart.getSoLuongTongCong() + ")");
    }



    // --- CÁC HÀM TRUY VẤN CSDL ---
    
    private List<String> getKhoHangNamesFromDB() {
        List<String> khoList = new ArrayList<>();
        String sql = "SELECT ten FROM khohang ORDER BY ten";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                khoList.add(rs.getString("ten"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return khoList;
    }
    
    private List<SanPham> getProductsByKhoName(String tenKho, int limit) {
        String sql = "SELECT sp.ma_san_pham, sp.ten, sp.don_gia, sp.so_luong, ncc.ten AS ten_ncc " +
                     "FROM sanpham sp " +
                     "LEFT JOIN nhacungcap ncc ON sp.ma_ncc = ncc.ma_ncc " +
                     "JOIN khohang kh ON sp.ma_kho = kh.ma_kho " +
                     "WHERE kh.ten = ? AND sp.so_luong > 0 " +
                     "ORDER BY sp.ten " +
                     (limit > 0 ? "LIMIT ?" : "");
        
        List<SanPham> products = new ArrayList<>();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenKho);
            if (limit > 0) {
                ps.setInt(2, limit);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    products.add(new SanPham(
                        rs.getString("ma_san_pham"), rs.getString("ten"),
                        rs.getLong("don_gia"), rs.getInt("so_luong"),
                        null, rs.getString("ten_ncc")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return products;
    }

    private List<SanPham> getProductsBySearchTerm(String searchTerm) {
         String sql = "SELECT sp.ma_san_pham, sp.ten, sp.don_gia, sp.so_luong, ncc.ten AS ten_ncc " +
                     "FROM sanpham sp " +
                     "LEFT JOIN nhacungcap ncc ON sp.ma_ncc = ncc.ma_ncc " +
                     "WHERE sp.ten ILIKE ? AND sp.so_luong > 0";
        
        List<SanPham> products = new ArrayList<>();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = ps.executeQuery()) {
                 while(rs.next()) {
                    products.add(new SanPham(
                        rs.getString("ma_san_pham"), rs.getString("ten"),
                        rs.getLong("don_gia"), rs.getInt("so_luong"),
                        null, rs.getString("ten_ncc")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return products;
    }
    
    private List<SanPham> getTopSellingProductsFromDB(int limit) {
        String sql = String.format(
            "SELECT sp.ma_san_pham, sp.ten, sp.don_gia, sp.so_luong, ncc.ten AS ten_ncc " +
            "FROM sanpham sp " +
            "LEFT JOIN nhacungcap ncc ON sp.ma_ncc = ncc.ma_ncc " +
            "JOIN (SELECT ma_san_pham, SUM(so_luong) AS total_sold " +
                  "FROM chitietxuat " +
                  "GROUP BY ma_san_pham " +
                  "ORDER BY total_sold DESC " +
                  "LIMIT %d) AS top_selling " +
            "ON sp.ma_san_pham = top_selling.ma_san_pham", limit);
        
        List<SanPham> list = new ArrayList<>();
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SanPham(
                    rs.getString("ma_san_pham"), rs.getString("ten"),
                    rs.getLong("don_gia"), rs.getInt("so_luong"),
                    null, rs.getString("ten_ncc")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private List<SanPham> getNewestProductsFromDB(int limit) {
        String sql = String.format(
            "SELECT sp.ma_san_pham, sp.ten, sp.don_gia, sp.so_luong, ncc.ten AS ten_ncc " +
            "FROM sanpham sp " +
            "LEFT JOIN nhacungcap ncc ON sp.ma_ncc = ncc.ma_ncc " +
            "WHERE sp.so_luong > 0 " +
            "ORDER BY sp.ma_san_pham DESC " +
            "LIMIT %d", limit);
        
        List<SanPham> list = new ArrayList<>();
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SanPham(
                    rs.getString("ma_san_pham"), rs.getString("ten"),
                    rs.getLong("don_gia"), rs.getInt("so_luong"),
                    null, rs.getString("ten_ncc")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    private SanPham getProductByIdFromDB(String maSP) {
        String sql = "SELECT sp.ma_san_pham, sp.ten, sp.don_gia, sp.so_luong, ncc.ten AS ten_ncc " +
                     "FROM sanpham sp " +
                     "LEFT JOIN nhacungcap ncc ON sp.ma_ncc = ncc.ma_ncc " +
                     "WHERE sp.ma_san_pham = ?";
        try(Connection conn = DatabaseConnector.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SanPham(
                        rs.getString("ma_san_pham"), rs.getString("ten"),
                        rs.getLong("don_gia"), rs.getInt("so_luong"),
                        null, rs.getString("ten_ncc")
                    );
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }
}