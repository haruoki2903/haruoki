package khohang;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import databaseconnector.DatabaseConnector;
import donnhaphang.DonNhapHangUI;
import mainui.MainUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhoHangUI extends JFrame {
    // --- Bảng màu và Font mới ---
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250);
    private static final Color COLOR_LOW_STOCK_BG = new Color(255, 244, 230);
    
    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_LIST_TITLE = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_LIST_KPI_TITLE = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_LIST_KPI_VALUE = new Font("Arial", Font.BOLD, 16);
    
    // --- Thành phần giao diện ---
    private JList<KhoInfo> listKhoHang;
    private DefaultListModel<KhoInfo> listModelKhoHang;
    private JTable tableSanPham;
    private DefaultTableModel modelSanPham;
    private Connection conn;
    private MainUI mainUiRef;

    public KhoHangUI(MainUI mainUi) {
        this.mainUiRef = mainUi;
        conn = DatabaseConnector.getConnection();
        
        setTitle("Tổng Quan Kho Hàng");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (conn != null && !conn.isClosed()) conn.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        initUI();
        loadKhoHangList();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BACKGROUND);
        setContentPane(mainPanel);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JScrollPane khoListScrollPane = createKhoListPanel();
        JPanel sanPhamPanel = createSanPhamPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, khoListScrollPane, sanPhamPanel);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.35);
        splitPane.setOpaque(false);
        splitPane.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));
        
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    public void setBorder(javax.swing.border.Border b) {}
                     @Override
                     public void paint(Graphics g) {
                         g.setColor(COLOR_BACKGROUND);
                         g.fillRect(0, 0, getWidth(), getHeight());
                     }
                };
            }
        });

        mainPanel.add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("TỔNG QUAN KHO HÀNG");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnToiTrangNhapHang = createStyledButton("Tới trang Nhập Hàng");
        btnToiTrangNhapHang.setBackground(COLOR_HEADER_TEXT);
        btnToiTrangNhapHang.setForeground(Color.WHITE);
        btnToiTrangNhapHang.addActionListener(e -> {
            // *** THAY ĐỔI: Truyền `this` (KhoHangUI) vào constructor của DonNhapHangUI
            new DonNhapHangUI(mainUiRef, this).setVisible(true);
        });
        JButton btnQuayLai = createStyledButton("Quay về");
        btnQuayLai.addActionListener(e -> dispose());
        buttonPanel.add(btnToiTrangNhapHang);
        buttonPanel.add(btnQuayLai);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JScrollPane createKhoListPanel() { 
        listModelKhoHang = new DefaultListModel<>();
        listKhoHang = new JList<>(listModelKhoHang);
        listKhoHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listKhoHang.setBackground(COLOR_BACKGROUND);
        listKhoHang.setCellRenderer(new KhoListCellRenderer());
        
        listKhoHang.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                KhoInfo selectedKho = listKhoHang.getSelectedValue();
                if (selectedKho != null) {
                    loadSanPhamTable(selectedKho.getMaKho());
                }
            }
        });
        
        JScrollPane khoListScrollPane = new JScrollPane(listKhoHang);
        khoListScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), "Danh sách Kho", 0, 0, FONT_TABLE_HEADER, COLOR_SECONDARY_TEXT));
        return khoListScrollPane;
    }
    
    private JPanel createSanPhamPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        
        String[] columns = {"Chọn", "Mã SP", "Tên Sản Phẩm", "Số Lượng", "Hạn Sử Dụng"};
        modelSanPham = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
            }
        };
        tableSanPham = new JTable(modelSanPham);
        
        styleTable();
        setupTableColumnWidths();

        JScrollPane scrollPane = new JScrollPane(tableSanPham);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        
        JButton btnAddToCart = createStyledButton("Thêm vào Danh sách Chờ nhập hàng");
        btnAddToCart.addActionListener(e -> themSanPhamVaoGioHang());
        bottomPanel.add(btnAddToCart);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), "Sản phẩm trong kho đã chọn", 0, 0, FONT_TABLE_HEADER, COLOR_SECONDARY_TEXT));
        
        return panel;
    }

    private void styleTable() {
        tableSanPham.setFont(FONT_TABLE_CELL);
        tableSanPham.setRowHeight(40);
        tableSanPham.setGridColor(COLOR_BORDER);
        tableSanPham.setSelectionBackground(COLOR_SELECTION);
        tableSanPham.setSelectionForeground(COLOR_PRIMARY_TEXT);
        tableSanPham.setFillsViewportHeight(true);

        JTableHeader header = tableSanPham.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_SECONDARY_TEXT);
        header.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        tableSanPham.setDefaultRenderer(Object.class, new SanPhamTableCellRenderer());
    }
    
    private void setupTableColumnWidths() {
        TableColumnModel columnModel = tableSanPham.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(120);
        columnModel.getColumn(2).setPreferredWidth(550);
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(4).setPreferredWidth(150);
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setForeground(COLOR_PRIMARY_TEXT);
        button.setBackground(COLOR_HEADER_BG);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), 
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        return button;
    }

    private void loadKhoHangList() { 
        listModelKhoHang.clear();
        String sql = "SELECT k.ma_kho, k.ten, " +
                     "COALESCE(SUM(CASE WHEN s.so_luong < 10 THEN 1 ELSE 0 END), 0) AS low_stock_count, " +
                     "COALESCE(SUM(CASE WHEN s.han_su_dung BETWEEN current_date AND current_date + INTERVAL '30 days' THEN 1 ELSE 0 END), 0) AS expiring_soon_count " +
                     "FROM khohang k LEFT JOIN sanpham s ON k.ma_kho = s.ma_kho " +
                     "GROUP BY k.ma_kho, k.ten ORDER BY k.ma_kho";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listModelKhoHang.addElement(new KhoInfo(
                    rs.getString("ma_kho"), rs.getString("ten"),
                    rs.getInt("low_stock_count"), rs.getInt("expiring_soon_count")));
            }
            if (!listModelKhoHang.isEmpty()) listKhoHang.setSelectedIndex(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách kho: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSanPhamTable(String maKho) { 
        modelSanPham.setRowCount(0);
        String sql = "SELECT ma_san_pham, ten, so_luong, han_su_dung FROM sanpham WHERE ma_kho = ? ORDER BY so_luong ASC, ten ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKho);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                modelSanPham.addRow(new Object[]{
                    false, rs.getString("ma_san_pham"), rs.getString("ten"),
                    rs.getInt("so_luong"), rs.getDate("han_su_dung")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void themSanPhamVaoGioHang() {
        List<Integer> selectedRows = new ArrayList<>();
        for (int i = 0; i < modelSanPham.getRowCount(); i++) {
            Boolean isSelected = (Boolean) modelSanPham.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                selectedRows.add(i);
            }
        }

        if (selectedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng tick chọn ít nhất một sản phẩm để thêm vào danh sách.",
                "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int addedCount = 0;
        NhapHangCart cart = NhapHangCart.getInstance();
        
        for (int row : selectedRows) {
            String maSP = modelSanPham.getValueAt(row, 1).toString();
            String tenSP = modelSanPham.getValueAt(row, 2).toString();
            int giaBanHienTai = 0;

            String sql = "SELECT don_gia FROM sanpham WHERE ma_san_pham = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maSP);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    giaBanHienTai = rs.getInt("don_gia");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lấy giá bán của sản phẩm " + tenSP, "Lỗi", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            NhapThongTinSanPhamDialog dialog = new NhapThongTinSanPhamDialog(this, tenSP, giaBanHienTai);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                int soLuongNhap = dialog.getSoLuong();
                int giaNhap = dialog.getGiaNhap();
                cart.addItem(maSP, tenSP, soLuongNhap, giaNhap);
                addedCount++;
            }
            
            modelSanPham.setValueAt(false, row, 0);
        }
        
        if (addedCount > 0) {
            JOptionPane.showMessageDialog(this,
                "Đã thêm " + addedCount + " sản phẩm vào danh sách chờ.\n" +
                "Tổng số sản phẩm đang chờ nhập: " + cart.getItemCount(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Tải lại toàn bộ dữ liệu trên giao diện Kho Hàng.
     * Bao gồm danh sách kho (cập nhật KPI) và bảng sản phẩm hiện tại.
     */
    public void refreshAllData() {
        int selectedIndex = listKhoHang.getSelectedIndex();
        
        loadKhoHangList();
        
        if (selectedIndex >= 0 && selectedIndex < listModelKhoHang.getSize()) {
            listKhoHang.setSelectedIndex(selectedIndex);
        } else if (!listModelKhoHang.isEmpty()) {
            listKhoHang.setSelectedIndex(0);
        }

        KhoInfo selectedKho = listKhoHang.getSelectedValue();
        if (selectedKho != null) {
            loadSanPhamTable(selectedKho.getMaKho());
        }
    }


    private static class KhoInfo {
        private final String maKho, tenKho;
        private final int lowStockCount, expiringSoonCount;

        public KhoInfo(String maKho, String tenKho, int lowStockCount, int expiringSoonCount) {
            this.maKho = maKho; this.tenKho = tenKho; this.lowStockCount = lowStockCount; this.expiringSoonCount = expiringSoonCount;
        }
        public String getMaKho() { return maKho; }
        public String getTenKho() { return tenKho; }
        public int getLowStockCount() { return lowStockCount; }
        public int getExpiringSoonCount() { return expiringSoonCount; }
    }

    class KhoListCellRenderer implements ListCellRenderer<KhoInfo> {
        private final JPanel mainPanel, kpiPanel;
        private final JLabel lblMaKho, lblTenKho;
        private final Border padding = BorderFactory.createEmptyBorder(15, 20, 15, 20);

        public KhoListCellRenderer() {
            mainPanel = new JPanel(new BorderLayout(20, 0));
            JPanel namePanel = new JPanel();
            namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
            namePanel.setOpaque(false);
            lblTenKho = new JLabel(); lblTenKho.setFont(FONT_LIST_TITLE); lblTenKho.setForeground(COLOR_PRIMARY_TEXT);
            lblMaKho = new JLabel(); lblMaKho.setFont(FONT_TABLE_CELL); lblMaKho.setForeground(COLOR_SECONDARY_TEXT);
            namePanel.add(lblTenKho);
            namePanel.add(Box.createRigidArea(new Dimension(0, 5)));
            namePanel.add(lblMaKho);
            mainPanel.add(namePanel, BorderLayout.CENTER);
            kpiPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            kpiPanel.setOpaque(false);
            mainPanel.add(kpiPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends KhoInfo> list, KhoInfo kho, int index, boolean isSelected, boolean cellHasFocus) {
            lblMaKho.setText(kho.getMaKho());
            lblTenKho.setText(kho.getTenKho());
            kpiPanel.removeAll();
            kpiPanel.add(createMiniKpi("Sắp hết", kho.getLowStockCount()));
            kpiPanel.add(createMiniKpi("Sắp HSD", kho.getExpiringSoonCount()));

            Color bgColor = isSelected ? COLOR_SELECTION : COLOR_HEADER_BG;
            mainPanel.setBackground(bgColor);
            mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER), padding));
            return mainPanel;
        }

        private JPanel createMiniKpi(String title, int value) {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JLabel valueLabel = new JLabel(String.valueOf(value));
            valueLabel.setFont(FONT_LIST_KPI_VALUE);
            valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(FONT_LIST_KPI_TITLE);
            titleLabel.setForeground(COLOR_SECONDARY_TEXT);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (value > 0) valueLabel.setForeground(new Color(200, 35, 51));
            else valueLabel.setForeground(COLOR_PRIMARY_TEXT);
            panel.add(valueLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 3)));
            panel.add(titleLabel);
            return panel;
        }
    }

    class SanPhamTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 2 || column == 3 || column == 4) setHorizontalAlignment(JLabel.CENTER);
            else if(column == 1) setHorizontalAlignment(JLabel.LEFT);
            setForeground(COLOR_PRIMARY_TEXT);
            
            if (isSelected) {
                setBackground(table.getSelectionBackground()); setForeground(table.getSelectionForeground());
            } else {
                boolean isWarning = false;
                int soLuong = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), 3);
                Object hsdObj = table.getModel().getValueAt(table.convertRowIndexToModel(row), 4);
                
                if (soLuong < 10) isWarning = true;
                if (!isWarning && hsdObj instanceof java.sql.Date) {
                    LocalDate hsd = ((java.sql.Date) hsdObj).toLocalDate();
                    if (!hsd.isBefore(LocalDate.now()) && hsd.isBefore(LocalDate.now().plusDays(30))) isWarning = true;
                }
                
                if (isWarning) setBackground(COLOR_LOW_STOCK_BG);
                else setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
            }
            return this;
        }
    }
}