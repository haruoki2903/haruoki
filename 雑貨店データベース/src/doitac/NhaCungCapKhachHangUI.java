package doitac;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import databaseconnector.DatabaseConnector;
import mainui.MainUI;

public class NhaCungCapKhachHangUI extends JFrame {
    // --- Bảng màu và Font mới, đồng bộ ---
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250);
    
    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TAB = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);

    // --- Components chung ---
    private Connection conn;
    private MainUI mainUiRef;
    private final Locale lc = new Locale("vi", "VN");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(lc);

    // --- Components cho tab Nhà Cung Cấp ---
    private JTable tableNCC, tableSP;
    private DefaultTableModel modelNCC, modelSP;
    private JTextField tfMaNCC, tfTenNCC, tfDiaChiNCC, tfSdtNCC;

    // --- Components cho tab Khách Hàng ---
    private JTable tableKH, tableDonXuat, tableChiTietXuat;
    private DefaultTableModel modelKH, modelDonXuat, modelChiTietXuat;
    
    public NhaCungCapKhachHangUI(MainUI mainUi) {
        this.mainUiRef = mainUi;
        conn = DatabaseConnector.getConnection();
        
        setTitle("Quản Lý Đối Tác");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try { if (conn != null && !conn.isClosed()) conn.close(); } 
                catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        initUI();
        loadDataNCC();
        loadDataKH();
    }

    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setBackground(COLOR_BACKGROUND);
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_TAB);
        tabbedPane.setOpaque(false);
        // <<<<< THAY ĐỔI: Không cần style thủ công nữa, FlatLaf sẽ làm đẹp hơn >>>>>

        tabbedPane.addTab("  Quản Lý Nhà Cung Cấp  ", createNhaCungCapTabPanel());
        tabbedPane.addTab("  Quản Lý Khách Hàng  ", createKhachHangTabPanel());
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));
        wrapper.setOpaque(false);
        wrapper.add(tabbedPane, BorderLayout.CENTER);

        contentPane.add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));
        
        JLabel titleLabel = new JLabel("QUẢN LÝ ĐỐI TÁC");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnBack = createStyledButton("Quay về");
        btnBack.addActionListener(e -> dispose());
        headerPanel.add(btnBack, BorderLayout.EAST);
        
        return headerPanel;
    }

    // =============================================================================
    // PANEL CHO TAB NHÀ CUNG CẤP
    // =============================================================================

    private JPanel createNhaCungCapTabPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JSplitPane splitPane = createStyledSplitPane(900);
        
        splitPane.setLeftComponent(createNccTablePanel());
        splitPane.setRightComponent(createNccDetailsPanel());
        
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createNccTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        String[] colNCC = {"Mã NCC", "Tên Nhà Cung Cấp", "Địa chỉ", "Số điện thoại"};
        modelNCC = new DefaultTableModel(colNCC, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableNCC = new JTable(modelNCC);
        styleTable(tableNCC);
        tableNCC.getColumnModel().getColumn(1).setPreferredWidth(300); // Tăng độ rộng tên NCC
        tableNCC.getColumnModel().getColumn(2).setPreferredWidth(350); // Tăng độ rộng địa chỉ
        
        tableNCC.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tableNCC.getSelectedRow();
                if (row != -1) {
                    tfMaNCC.setText(modelNCC.getValueAt(row, 0).toString());
                    tfTenNCC.setText(modelNCC.getValueAt(row, 1).toString());
                    tfDiaChiNCC.setText(modelNCC.getValueAt(row, 2) != null ? modelNCC.getValueAt(row, 2).toString() : "");
                    tfSdtNCC.setText(modelNCC.getValueAt(row, 3) != null ? modelNCC.getValueAt(row, 3).toString() : "");
                    loadSanPhamTheoNCC(tfMaNCC.getText());
                    tfMaNCC.setEditable(false);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableNCC);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createNccDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panel.setBackground(COLOR_HEADER_BG);
        
        panel.add(createNccInputPanel(), BorderLayout.NORTH);
        panel.add(createSpTablePanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNccInputPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Thông tin chi tiết Nhà Cung Cấp"),
            BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        row = addFormField(formPanel, gbc, row, "Mã NCC (*):", tfMaNCC = new JTextField());
        row = addFormField(formPanel, gbc, row, "Tên NCC (*):", tfTenNCC = new JTextField());
        row = addFormField(formPanel, gbc, row, "Địa chỉ:", tfDiaChiNCC = new JTextField());
        row = addFormField(formPanel, gbc, row, "Số điện thoại:", tfSdtNCC = new JTextField());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnAddNCC = createStyledButton("Thêm");
        btnAddNCC.setBackground(COLOR_HEADER_TEXT);
        btnAddNCC.setForeground(Color.WHITE);
        buttonPanel.add(btnAddNCC);
        
        JButton btnUpdateNCC = createStyledButton("Sửa");
        buttonPanel.add(btnUpdateNCC);
        
        JButton btnDeleteNCC = createStyledButton("Xóa");
        buttonPanel.add(btnDeleteNCC);

        JButton btnRefreshNCC = createStyledButton("Làm mới");
        buttonPanel.add(btnRefreshNCC);
        
        btnAddNCC.addActionListener(e -> addSupplier());
        btnUpdateNCC.addActionListener(e -> updateSupplier());
        btnDeleteNCC.addActionListener(e -> deleteSupplier());
        btnRefreshNCC.addActionListener(e -> clearInputFieldsNCC());
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 8, 0, 8);
        formPanel.add(buttonPanel, gbc);
        
        return formPanel;
    }
    
    private JPanel createSpTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Sản phẩm được cung cấp"));
        
        String[] colSP = {"Mã SP", "Tên Sản Phẩm", "Tồn kho"};
        modelSP = new DefaultTableModel(colSP, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        tableSP = new JTable(modelSP);
        styleTable(tableSP);
        tableSP.getColumnModel().getColumn(1).setPreferredWidth(300);
        tableSP.getColumnModel().getColumn(2).setCellRenderer(new CenterRenderer());

        JScrollPane scrollPane = new JScrollPane(tableSP);
        scrollPane.getViewport().setBackground(COLOR_HEADER_BG);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    // =============================================================================
    // PANEL CHO TAB KHÁCH HÀNG
    // =============================================================================
    private JPanel createKhachHangTabPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JSplitPane mainSplitPane = createStyledSplitPane(700);

        mainSplitPane.setLeftComponent(createKhachHangListPanel());
        
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setDividerLocation(350);
        rightSplitPane.setOpaque(false);
        rightSplitPane.setBorder(null);
        rightSplitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() { return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) { public void setBorder(javax.swing.border.Border b) {} }; }
        });
        
        rightSplitPane.setTopComponent(createDonXuatPanel());
        rightSplitPane.setBottomComponent(createChiTietXuatPanel());
        
        mainSplitPane.setRightComponent(rightSplitPane);
        panel.add(mainSplitPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createKhachHangListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        
        JLabel title = createSectionTitle("Danh sách Khách Hàng");
        panel.add(title, BorderLayout.NORTH);

        String[] colKH = {"Mã KH", "Tên Khách Hàng", "SĐT", "Tổng chi tiêu"};
        modelKH = new DefaultTableModel(colKH, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableKH = new JTable(modelKH);
        styleTable(tableKH);
        tableKH.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableKH.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer());

        tableKH.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableKH.getSelectedRow();
                if (row >= 0) { loadDonXuatByKhachHang(modelKH.getValueAt(row, 0).toString()); }
            }
        });
        JScrollPane scrollPane = new JScrollPane(tableKH);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnAddKH = createStyledButton("Thêm mới");
        btnAddKH.setBackground(COLOR_HEADER_TEXT); btnAddKH.setForeground(Color.WHITE);
        JButton btnUpdateKH = createStyledButton("Sửa");
        JButton btnDeleteKH = createStyledButton("Xóa");
        
        btnAddKH.addActionListener(e -> showKhachHangDialog(null));
        btnUpdateKH.addActionListener(e -> {
            int row = tableKH.getSelectedRow();
            if (row >= 0) showKhachHangDialog(tableKH.getValueAt(row, 0).toString());
            else JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để sửa.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        btnDeleteKH.addActionListener(e -> deleteCustomer());
        
        buttonPanel.add(btnAddKH);
        buttonPanel.add(btnUpdateKH);
        buttonPanel.add(btnDeleteKH);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createDonXuatPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        JLabel title = createSectionTitle("Lịch sử hóa đơn của khách hàng đã chọn");
        panel.add(title, BorderLayout.NORTH);

        String[] colDX = {"Mã Hóa Đơn", "Ngày Xuất", "Tổng Tiền"};
        modelDonXuat = new DefaultTableModel(colDX, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableDonXuat = new JTable(modelDonXuat);
        styleTable(tableDonXuat);
        tableDonXuat.getColumnModel().getColumn(2).setCellRenderer(new CurrencyRenderer());

        tableDonXuat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableDonXuat.getSelectedRow();
                if (row >= 0) { loadChiTietXuatByDonHang(modelDonXuat.getValueAt(row, 0).toString()); }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableDonXuat);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createChiTietXuatPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 0));
        
        JLabel title = createSectionTitle("Chi tiết sản phẩm trong hóa đơn");
        panel.add(title, BorderLayout.NORTH);

        String[] colCTX = {"STT", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá", "Thành Tiền"};
        modelChiTietXuat = new DefaultTableModel(colCTX, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableChiTietXuat = new JTable(modelChiTietXuat);
        styleTable(tableChiTietXuat);
        
        TableColumnModel tcm = tableChiTietXuat.getColumnModel();
        tcm.getColumn(0).setMaxWidth(60);
        tcm.getColumn(0).setCellRenderer(new CenterRenderer());
        tcm.getColumn(1).setPreferredWidth(300);
        tcm.getColumn(2).setCellRenderer(new CenterRenderer());
        tcm.getColumn(3).setCellRenderer(new CurrencyRenderer());
        tcm.getColumn(4).setCellRenderer(new CurrencyRenderer());
        
        JScrollPane scrollPane = new JScrollPane(tableChiTietXuat);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JSplitPane createStyledSplitPane(int dividerLocation) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(dividerLocation);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
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
        return splitPane;
    }
    
    private int addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        JLabel label = createStyledLabel(labelText);
        component.setFont(FONT_TABLE_CELL);
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
        return row + 1;
    }
    
    private JLabel createSectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(FONT_TAB);
        title.setForeground(COLOR_PRIMARY_TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
        return title;
    }
    
    private void styleTable(JTable table) {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(40);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_PRIMARY_TEXT);
        table.setFillsViewportHeight(true);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_SECONDARY_TEXT);
        header.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_PRIMARY_TEXT);
        return label;
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // --- Logic NCC ---
    private void clearInputFieldsNCC() {
        tfMaNCC.setText("");
        tfTenNCC.setText("");
        tfDiaChiNCC.setText("");
        tfSdtNCC.setText("");
        tfMaNCC.setEditable(true);
        tableNCC.clearSelection();
        modelSP.setRowCount(0);
    }
    private void loadDataNCC() {
        modelNCC.setRowCount(0);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM nhacungcap ORDER BY ten")) {
            while (rs.next()) {
                modelNCC.addRow(new Object[]{ rs.getString("ma_ncc"), rs.getString("ten"), rs.getString("dia_chi"), rs.getString("so_dien_thoai") });
            }
        } catch (SQLException e) { showError("Lỗi tải nhà cung cấp: " + e.getMessage()); }
    }
    private void loadSanPhamTheoNCC(String maNCC) {
        modelSP.setRowCount(0);
        String sql = "SELECT ma_san_pham, ten, so_luong FROM sanpham WHERE ma_ncc = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maNCC);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                modelSP.addRow(new Object[]{ rs.getString("ma_san_pham"), rs.getString("ten"), rs.getInt("so_luong") });
            }
        } catch (SQLException e) { showError("Lỗi tải sản phẩm: " + e.getMessage()); }
    }
    private void addSupplier() { 
        if (tfMaNCC.getText().trim().isEmpty() || tfTenNCC.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã và Tên NCC không được trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "INSERT INTO nhacungcap (ma_ncc, ten, dia_chi, so_dien_thoai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tfMaNCC.getText()); 
            pstmt.setString(2, tfTenNCC.getText());
            pstmt.setString(3, tfDiaChiNCC.getText());
            pstmt.setString(4, tfSdtNCC.getText());
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!");
            loadDataNCC();
            clearInputFieldsNCC();
        } catch (SQLException e) { 
            if(e.getSQLState().equals("23505")) showError("Lỗi: Mã nhà cung cấp đã tồn tại.");
            else showError("Lỗi thêm NCC: " + e.getMessage()); 
        }
    }
    private void updateSupplier() {
        if (tableNCC.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Chọn một nhà cung cấp để sửa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "UPDATE nhacungcap SET ten=?, dia_chi=?, so_dien_thoai=? WHERE ma_ncc=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tfTenNCC.getText());
            pstmt.setString(2, tfDiaChiNCC.getText());
            pstmt.setString(3, tfSdtNCC.getText());
            pstmt.setString(4, tfMaNCC.getText());
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataNCC();
            clearInputFieldsNCC();
        } catch (SQLException e) { showError("Lỗi sửa NCC: " + e.getMessage()); }
    }
    private void deleteSupplier() {
        if (tableNCC.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Chọn một nhà cung cấp để xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maNCC = tfMaNCC.getText();
        try {
            String checkSql = "SELECT 1 FROM sanpham WHERE ma_ncc = ? LIMIT 1";
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setString(1, maNCC);
                if (checkPstmt.executeQuery().next()) {
                    showError("Không thể xóa NCC vì vẫn còn sản phẩm được liên kết."); return;
                }
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa NCC '" + maNCC + "'?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM nhacungcap WHERE ma_ncc=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, maNCC); 
                    pstmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Xóa nhà cung cấp thành công!");
                    loadDataNCC();
                    clearInputFieldsNCC();
                }
            }
        } catch (SQLException e) { showError("Lỗi xóa NCC: " + e.getMessage()); }
    }

    // --- Logic Khách Hàng ---
    private void loadDataKH() {
        modelKH.setRowCount(0);
        String sql = "SELECT ma_khach, ten, so_dien_thoai, tong_gia_tri_mua FROM khachhang ORDER BY ten";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelKH.addRow(new Object[]{
                    rs.getString("ma_khach"), rs.getString("ten"),
                    rs.getString("so_dien_thoai"), rs.getLong("tong_gia_tri_mua")
                });
            }
        } catch (SQLException e) {
            showError("Lỗi tải khách hàng: " + e.getMessage());
        }
    }
    
    private void loadDonXuatByKhachHang(String maKH) {
        modelDonXuat.setRowCount(0);
        modelChiTietXuat.setRowCount(0);
        String sql = "SELECT dx.ma_xuat, dx.ngay_xuat, dx.tong_tien FROM donxuat dx WHERE dx.ma_khach = ? ORDER BY dx.ngay_xuat DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKH);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                modelDonXuat.addRow(new Object[]{
                    rs.getString("ma_xuat"), rs.getDate("ngay_xuat"), rs.getLong("tong_tien")
                });
            }
        } catch (SQLException e) {
            showError("Lỗi tải hóa đơn của khách hàng: " + e.getMessage());
        }
    }

    private void loadChiTietXuatByDonHang(String maXuat) {
        modelChiTietXuat.setRowCount(0);
        String sql = "SELECT ROW_NUMBER() OVER (ORDER BY sp.ten) as stt, sp.ten, ctx.so_luong, sp.don_gia, (ctx.so_luong * sp.don_gia) AS thanh_tien FROM chitietxuat ctx JOIN sanpham sp ON ctx.ma_san_pham = sp.ma_san_pham WHERE ctx.ma_xuat = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maXuat);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                modelChiTietXuat.addRow(new Object[]{
                    rs.getLong("stt"), rs.getString("ten"), rs.getInt("so_luong"),
                    rs.getLong("don_gia"), rs.getLong("thanh_tien")
                });
            }
        } catch (SQLException e) {
            showError("Lỗi tải chi tiết hóa đơn: " + e.getMessage());
        }
    }

    private void showKhachHangDialog(String maKHToEdit) {
        JDialog dialog = new JDialog(this, "Thông tin Khách hàng", true);
        dialog.setSize(500, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_BACKGROUND);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField tfMa = new JTextField(20);
        JTextField tfTen = new JTextField(20);
        JTextField tfSdt = new JTextField(20);

        int row = 0;
        row = addFormField(formPanel, gbc, row, "Mã KH (*):", tfMa);
        row = addFormField(formPanel, gbc, row, "Tên KH (*):", tfTen);
        row = addFormField(formPanel, gbc, row, "Số điện thoại:", tfSdt);
        
        if (maKHToEdit != null) {
            dialog.setTitle("Cập nhật Khách hàng");
            tfMa.setText(maKHToEdit);
            tfMa.setEditable(false);
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT ten, so_dien_thoai FROM khachhang WHERE ma_khach = ?")) {
                pstmt.setString(1, maKHToEdit);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()){
                    tfTen.setText(rs.getString("ten"));
                    tfSdt.setText(rs.getString("so_dien_thoai"));
                }
            } catch (SQLException e) { showError("Lỗi tải thông tin KH: "+e.getMessage()); }
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        
        JButton btnSave = createStyledButton("Lưu lại");
        btnSave.setBackground(COLOR_HEADER_TEXT);
        btnSave.setForeground(Color.WHITE);

        JButton btnCancel = createStyledButton("Hủy");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        btnSave.addActionListener(e -> {
            String ma = tfMa.getText().trim();
            String ten = tfTen.getText().trim();
            String sdtStr = tfSdt.getText().trim();
            
            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Mã và Tên khách hàng không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (maKHToEdit == null) {
                    String sql = "INSERT INTO khachhang (ma_khach, ten, so_dien_thoai) VALUES (?, ?, ?)";
                    try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, ma);
                        pstmt.setString(2, ten);
                        pstmt.setString(3, sdtStr);
                        pstmt.executeUpdate();
                    }
                } else {
                    String sql = "UPDATE khachhang SET ten=?, so_dien_thoai=? WHERE ma_khach=?";
                    try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                        pstmt.setString(1, ten);
                        pstmt.setString(2, sdtStr);
                        pstmt.setString(3, maKHToEdit);
                        pstmt.executeUpdate();
                    }
                }
                
                loadDataKH();
                if(mainUiRef != null) {
                    mainUiRef.refreshKpis();
                }

                JOptionPane.showMessageDialog(dialog, "Thao tác thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

            } catch (SQLException ex) {
                if (ex.getSQLState().equals("23505")) { showError("Lỗi: Mã khách hàng '" + ma + "' đã tồn tại!");
                } else { showError("Lỗi lưu khách hàng: " + ex.getMessage()); }
            }
        });
        
        btnCancel.addActionListener(ev -> dialog.dispose());

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void deleteCustomer() {
        int selectedRow = tableKH.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maKH = tableKH.getValueAt(selectedRow, 0).toString();
        String tenKH = tableKH.getValueAt(selectedRow, 1).toString();
        
        try {
            String checkSql = "SELECT 1 FROM donxuat WHERE ma_khach = ? LIMIT 1";
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setString(1, maKH);
                if (checkPstmt.executeQuery().next()) {
                    showError("Không thể xóa khách hàng '" + tenKH + "' vì đã có lịch sử mua hàng.");
                    return;
                }
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khách hàng '" + tenKH + "' không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM khachhang WHERE ma_khach=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, maKH);
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        
                        loadDataKH();
                        if(mainUiRef != null) {
                             mainUiRef.refreshKpis();
                        }
                        
                        modelDonXuat.setRowCount(0);
                        modelChiTietXuat.setRowCount(0);
                    }
                }
            }
        } catch (SQLException e) {
            showError("Lỗi xóa khách hàng: " + e.getMessage());
        }
    }

    // --- Inner Classes ---
    class CustomTableCellRenderer extends DefaultTableCellRenderer {
        public CustomTableCellRenderer() { setHorizontalAlignment(JLabel.CENTER); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
                setForeground(COLOR_PRIMARY_TEXT);
            }
            if (col == 1) setHorizontalAlignment(JLabel.LEFT);
            else if(col == 2 && table != tableKH) setHorizontalAlignment(JLabel.LEFT);
            else setHorizontalAlignment(JLabel.CENTER);

            return this;
        }
    }

    class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() { setHorizontalAlignment(JLabel.RIGHT); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
             if (value instanceof Number) {
                setText(currencyFormatter.format(value) + "   ");
            } else {
                setText(value != null ? value.toString() : "");
            }
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
                setForeground(COLOR_PRIMARY_TEXT);
            }
            return this;
        }
    }
    class CenterRenderer extends DefaultTableCellRenderer {
        public CenterRenderer() { setHorizontalAlignment(JLabel.CENTER); }
         @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
             if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
                setForeground(COLOR_PRIMARY_TEXT);
            }
            return this;
         }
    }
}