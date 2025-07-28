package chitietxuathang;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import databaseconnector.DatabaseConnector;
import donxuathang.DonXuatUI;
import mainui.MainUI;


public class ChiTietXuatUI extends JFrame {
    // <<<< BẢNG MÀU VÀ FONT MỚI >>>>
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250);
    
    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    
    // --- Các thành phần giao diện ---
    private JTable tableSP;
    private DefaultTableModel modelSP;
    private JComboBox<SanPhamInfo> comboBoxSP;
    private JTextField tfSoLuong, tfTonKho;

    private String maXuat;
    private Connection conn;
    private List<SanPhamInfo> allSanPhams;
    private MainUI mainUiRef;
    private DonXuatUI donXuatUiRef;

    public ChiTietXuatUI(MainUI mainUi, DonXuatUI donXuatUi, String maXuat) {
        this.mainUiRef = mainUi;
        this.donXuatUiRef = donXuatUi;
        this.maXuat = maXuat;
        conn = DatabaseConnector.getConnection();
        allSanPhams = new ArrayList<>();

        setTitle("Chi Tiết Đơn Xuất Hàng: " + maXuat);
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (donXuatUiRef != null) donXuatUiRef.refreshData();
                if (mainUiRef != null) mainUiRef.refreshKpis();
                try { if (conn != null && !conn.isClosed()) conn.close(); }
                catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        initUI();
        loadAllSanPhamData();
        loadData();
    }

    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setBackground(COLOR_BACKGROUND);
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, tablePanel);
        splitPane.setDividerLocation(450);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 25, 15, 25));
        splitPane.setOpaque(false);
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

        contentPane.add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("CHI TIẾT ĐƠN XUẤT HÀNG: " + this.maXuat.toUpperCase());
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton btnQuayLai = createSecondaryButton("Hoàn tất & Quay lại");
        
        // <<<< THAY ĐỔI Ở ĐÂY >>>>
        // Thay vì chỉ gọi dispose(), hãy gọi các phương thức refresh trước.
        btnQuayLai.addActionListener(e -> {
            // 1. Refresh dữ liệu ở màn hình DonXuatUI (cập nhật tổng tiền,...)
            if (donXuatUiRef != null) {
                donXuatUiRef.refreshData();
            }
            // 2. Refresh KPI ở màn hình chính (nếu có thay đổi)
            if (mainUiRef != null) {
                mainUiRef.refreshKpis();
            }
            // 3. Đóng cửa sổ hiện tại
            dispose();
        });
        
        headerPanel.add(btnQuayLai, BorderLayout.EAST);

        return headerPanel;
    }

    
    private JPanel createFormPanel() {
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(createStyledLabel("Tìm & Chọn sản phẩm:"), gbc);
        
        gbc.gridy = row++;
        comboBoxSP = new JComboBox<>();
        comboBoxSP.setEditable(true);
        comboBoxSP.setFont(FONT_TABLE_CELL);
        formPanel.add(comboBoxSP, gbc);
        configureAutocomplete();

        gbc.gridy = row++; gbc.insets = new Insets(20, 5, 10, 5);
        formPanel.add(new JSeparator(), gbc);
        
        row = addFormField(formPanel, gbc, row, "Số lượng tồn kho:", tfTonKho = new JTextField());
        tfTonKho.setEditable(false);

        row = addFormField(formPanel, gbc, row, "Số lượng xuất (*):", tfSoLuong = new JTextField());
        
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.insets = new Insets(20, 5, 0, 5);
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;

        // <<<< THAY ĐỔI 2: Sử dụng createPrimaryButton cho nút chính và bỏ các dòng set màu thủ công >>>>
        JButton btnXacNhan = createPrimaryButton("Thêm Vào Đơn");
        btnXacNhan.addActionListener(e -> themSanPhamVaoDon());
        formPanel.add(btnXacNhan, gbc);
        
        gbc.gridy = row; gbc.weighty = 1.0;
        formPanel.add(new JLabel(), gbc);

        formWrapper.add(formPanel, BorderLayout.CENTER);
        return formWrapper;
    }

    private int addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        JLabel label = createStyledLabel(labelText);
        component.setFont(FONT_TABLE_CELL);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        panel.add(label, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        panel.add(component, gbc);
        
        return row + 1;
    }

    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout(0, 15));
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        String[] cols = {"Mã SP", "Tên Sản Phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        modelSP = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableSP = new JTable(modelSP);
        styleTable();
        tableContainer.add(new JScrollPane(tableSP), BorderLayout.CENTER);
        
        // <<<< THAY ĐỔI 3: Sử dụng createSecondaryButton cho nút phụ >>>>
        JButton btnXoa = createSecondaryButton("Xóa sản phẩm đã chọn");
        btnXoa.addActionListener(e -> xoaSanPhamKhoiDon());
        JPanel deleteButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        deleteButtonPanel.setOpaque(false);
        deleteButtonPanel.add(btnXoa);

        tableContainer.add(deleteButtonPanel, BorderLayout.SOUTH);
        return tableContainer;
    }

    private void styleTable() {
        tableSP.setFont(FONT_TABLE_CELL);
        tableSP.setRowHeight(40);
        tableSP.setGridColor(COLOR_BORDER);
        tableSP.getTableHeader().setFont(FONT_TABLE_HEADER);
        tableSP.getTableHeader().setBackground(COLOR_HEADER_BG);
        tableSP.getTableHeader().setForeground(COLOR_SECONDARY_TEXT);
        tableSP.getTableHeader().setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        tableSP.setSelectionBackground(COLOR_SELECTION);
        tableSP.setSelectionForeground(COLOR_PRIMARY_TEXT);      
        TableColumnModel columnModel = tableSP.getColumnModel();
        columnModel.getColumn(0).setCellRenderer(new CenterRenderer());
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(350);
        columnModel.getColumn(2).setCellRenderer(new CenterRenderer());
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setCellRenderer(new CurrencyRenderer());
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setCellRenderer(new CurrencyRenderer());
        columnModel.getColumn(4).setPreferredWidth(120);
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(COLOR_PRIMARY_TEXT); // Thêm màu cho label
        return label;
    }

    // <<<< THAY ĐỔI 4: Đổi tên createStyledButton thành createSecondaryButton và sửa lại font >>>>
    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON); // Sử dụng FONT_BUTTON cho nhất quán
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(COLOR_PRIMARY_TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), // Dùng màu border chung
            BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
        return button;
    }

    // <<<< THAY ĐỔI 5: Thêm phương thức mới cho nút hành động chính >>>>
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setFocusPainted(false);
        
        final Color originalBg = COLOR_HEADER_TEXT; // Màu nền tối
        final Color hoverBg = originalBg.brighter(); // Màu sáng hơn khi hover
        
        button.setBackground(originalBg);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(originalBg), 
            BorderFactory.createEmptyBorder(10, 20, 10, 20)));
            
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hoverBg); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(originalBg); }
        });
        return button;
    }
    
    private void loadAllSanPhamData() {
        allSanPhams.clear();
        String sql = "SELECT ma_san_pham, ten, so_luong FROM sanpham WHERE so_luong > 0 ORDER BY ten";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allSanPhams.add(new SanPhamInfo(
                    rs.getString("ma_san_pham"), rs.getString("ten"), rs.getInt("so_luong")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureAutocomplete() {
        JTextField editor = (JTextField) comboBoxSP.getEditor().getEditorComponent();
        comboBoxSP.addActionListener(e -> {
            Object selectedItem = comboBoxSP.getSelectedItem();
            if (e.getActionCommand().equals("comboBoxEdited")) { return; }
            if (selectedItem instanceof SanPhamInfo) {
                SanPhamInfo selectedSP = (SanPhamInfo) selectedItem;
                tfTonKho.setText(String.valueOf(selectedSP.getSoLuongTon()));
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String text = editor.getText();
                    if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_ENTER) { return; }
                    if (text.isEmpty()) { comboBoxSP.hidePopup(); tfTonKho.setText(""); return; }
                    DefaultComboBoxModel<SanPhamInfo> model = new DefaultComboBoxModel<>();
                    for (SanPhamInfo sp : allSanPhams) {
                        if (sp.getTen().toLowerCase().contains(text.toLowerCase())) { model.addElement(sp); }
                    }
                    comboBoxSP.setModel(model);
                    comboBoxSP.setPopupVisible(model.getSize() > 0);
                    editor.setText(text);
                });
            }
        });
    }

    private void loadData() {
        modelSP.setRowCount(0);
        String sql = "SELECT c.ma_san_pham, s.ten, c.so_luong, s.don_gia FROM chitietxuat c JOIN sanpham s ON c.ma_san_pham = s.ma_san_pham WHERE c.ma_xuat = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maXuat);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelSP.addRow(new Object[]{
                    rs.getString("ma_san_pham"), rs.getString("ten"),
                    rs.getInt("so_luong"), rs.getInt("don_gia"),
                    (long) rs.getInt("so_luong") * rs.getInt("don_gia")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết xuất: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void themSanPhamVaoDon() {
        Object selectedItem = comboBoxSP.getSelectedItem();
        if (!(selectedItem instanceof SanPhamInfo)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm hợp lệ từ danh sách gợi ý.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SanPhamInfo selectedSP = (SanPhamInfo) selectedItem;
        String maSP = selectedSP.getMaSanPham();
        int soLuongXuat;

        try {
            soLuongXuat = Integer.parseInt(tfSoLuong.getText().trim());
            if (soLuongXuat <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng xuất phải là số nguyên dương.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng xuất không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO chitietxuat (ma_xuat, ma_san_pham, so_luong) VALUES (?, ?, ?)";
        try (PreparedStatement psInsert = conn.prepareStatement(sql)) {
            psInsert.setString(1, maXuat);
            psInsert.setString(2, maSP);
            psInsert.setInt(3, soLuongXuat);
            psInsert.executeUpdate();
            
            if (mainUiRef != null) mainUiRef.refreshKpis();
            JOptionPane.showMessageDialog(this, "Đã thêm sản phẩm vào đơn xuất!");
            loadData();
            loadAllSanPhamData();
            
            SwingUtilities.invokeLater(() -> {
                comboBoxSP.setSelectedItem(null); tfSoLuong.setText(""); tfTonKho.setText("");
                ((JTextField)comboBoxSP.getEditor().getEditorComponent()).setText("");
            });
            
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("KHONG DU HANG")) {
                JOptionPane.showMessageDialog(this, "Lỗi từ CSDL: " + e.getMessage(), "Không đủ hàng", JOptionPane.ERROR_MESSAGE);
            } else if (e.getSQLState().equals("23505")) {
                 JOptionPane.showMessageDialog(this, "Sản phẩm này đã có trong đơn hàng!", "Lỗi Trùng lặp", JOptionPane.ERROR_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Lỗi khi thêm sản phẩm: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xoaSanPhamKhoiDon() {
        int selectedRow = tableSP.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa khỏi đơn.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maSP = tableSP.getValueAt(selectedRow, 0).toString();

        String sql = "DELETE FROM chitietxuat WHERE ma_xuat = ? AND ma_san_pham = ?";
        try (PreparedStatement psDelete = conn.prepareStatement(sql)) {
            psDelete.setString(1, maXuat);
            psDelete.setString(2, maSP);
            int rowsAffected = psDelete.executeUpdate();
            
            if (rowsAffected > 0) {
                if (mainUiRef != null) mainUiRef.refreshKpis();
                JOptionPane.showMessageDialog(this, "Đã xóa sản phẩm khỏi đơn hàng!");
                loadData();
                loadAllSanPhamData();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatTongTienVaKhachHang() { /* Đã được xử lý bởi Trigger */ }
    
    // --- Các lớp Inner Class ---
    class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() { setHorizontalAlignment(JLabel.RIGHT); }
        @Override
        public void setValue(Object value) {
            if (value instanceof Number) {
                setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(value));
            } else { super.setValue(value); }
        }
    }
    class CenterRenderer extends DefaultTableCellRenderer { public CenterRenderer() { setHorizontalAlignment(JLabel.CENTER); } }
    
    private static class SanPhamInfo {
        private final String maSanPham, ten; private final int soLuongTon;
        public SanPhamInfo(String maSanPham, String ten, int soLuongTon) { this.maSanPham = maSanPham; this.ten = ten; this.soLuongTon = soLuongTon; }
        public String getMaSanPham() { return maSanPham; }
        public String getTen() { return ten; }
        public int getSoLuongTon() { return soLuongTon; }
        @Override public String toString() { return ten; }
    }
}