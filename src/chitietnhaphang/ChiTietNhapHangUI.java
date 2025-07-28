package chitietnhaphang;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import databaseconnector.DatabaseConnector;
import donnhaphang.DonNhapHangUI;
import mainui.MainUI;
import java.util.Map;

public class ChiTietNhapHangUI extends JFrame {
    // <<<< BẢNG MÀU VÀ FONT MỚI >>>>
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250);
    private static final Color COLOR_WARNING_BG = new Color(255, 244, 230);
    private static final Color COLOR_WARNING_FG = new Color(255, 107, 107);
    
    
    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);

    // --- Các thành phần giao diện ---
    private JTable tableSP;
    private DefaultTableModel modelSP;
    private JComboBox<SanPhamInfo> comboBoxSP;
    private JTextField tfSoLuong, tfDonGia, tfDonGiaBan, tfTonKho;

    private String maNhap;
    private Connection conn;
    private List<SanPhamInfo> allSanPhams;
    private MainUI mainUiRef;
    private DonNhapHangUI donNhapHangUiRef;
    
    public ChiTietNhapHangUI(MainUI mainUi, DonNhapHangUI donNhapUi, String maNhap) {
        this.mainUiRef = mainUi;
        this.donNhapHangUiRef = donNhapUi;
        this.maNhap = maNhap;
        conn = DatabaseConnector.getConnection();
        allSanPhams = new ArrayList<>();

        setTitle("Chi Tiết Đơn Nhập Hàng: " + maNhap);
        setSize(1280, 800); // Tăng chiều cao một chút
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (donNhapHangUiRef != null) donNhapHangUiRef.refreshData();
                if (mainUiRef != null) mainUiRef.refreshKpis();
                try { if (conn != null && !conn.isClosed()) conn.close(); } 
                catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        initUI();
        loadAllSanPhamData();
        loadChiTietSP();
    }

    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setBackground(COLOR_BACKGROUND);
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, tablePanel);
        splitPane.setDividerLocation(450); // Điều chỉnh
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 25, 15, 25)); // Padding cho split pane
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

        JLabel titleLabel = new JLabel("CHI TIẾT ĐƠN NHẬP HÀNG: " + this.maNhap.toUpperCase());
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton btnQuayLai = createStyledButton("Hoàn tất & Quay lại");
        btnQuayLai.addActionListener(e -> dispose());
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
        comboBoxSP.setRenderer(new SanPhamComboBoxRenderer());
        formPanel.add(comboBoxSP, gbc);
        configureAutocomplete();
        
        // ... (các trường nhập liệu khác được thêm vào tương tự) ...

        gbc.gridy = row++; gbc.insets = new Insets(20, 5, 10, 5);
        formPanel.add(new JSeparator(), gbc);
        
        row = addFormField(formPanel, gbc, row, "Giá bán hiện tại:", tfDonGiaBan = new JTextField());
        tfDonGiaBan.setFont(new Font("Arial", Font.BOLD, 16));
        tfDonGiaBan.setEditable(false); tfDonGiaBan.setHorizontalAlignment(JTextField.RIGHT);
        
        row = addFormField(formPanel, gbc, row, "Số lượng tồn kho:", tfTonKho = new JTextField());
        tfTonKho.setFont(FONT_TABLE_CELL);
        tfTonKho.setEditable(false);

        row = addFormField(formPanel, gbc, row, "Đơn giá nhập mới (*):", tfDonGia = new JTextField());
        row = addFormField(formPanel, gbc, row, "Số lượng nhập (*):", tfSoLuong = new JTextField());
        
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.insets = new Insets(20, 5, 0, 5);
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnXacNhan = createStyledButton("Thêm Vào Đơn");
        btnXacNhan.setBackground(COLOR_HEADER_TEXT);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.addActionListener(e -> themSanPhamVaoDon());
        formPanel.add(btnXacNhan, gbc);
        
        // Panel rỗng để đẩy mọi thứ lên trên
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
        tableContainer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // Padding trái
        
        String[] cols = {"Mã SP", "Tên Sản Phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        modelSP = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableSP = new JTable(modelSP);
        styleTable();
        tableContainer.add(new JScrollPane(tableSP), BorderLayout.CENTER);
        
        JButton btnXoa = createStyledButton("Xóa sản phẩm đã chọn");
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
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(350);
        columnModel.getColumn(2).setCellRenderer(centerRenderer);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setCellRenderer(new CurrencyRenderer());
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setCellRenderer(new CurrencyRenderer());
        columnModel.getColumn(4).setPreferredWidth(120);
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(240, 240, 240)); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
        return button;
    }
    
    private void loadAllSanPhamData() {
        allSanPhams.clear();
        String sql = "SELECT ma_san_pham, ten, don_gia, so_luong, han_su_dung, CASE WHEN so_luong < 10 THEN 1 WHEN han_su_dung BETWEEN current_date AND current_date + INTERVAL '30 days' THEN 2 ELSE 3 END as priority FROM sanpham ORDER BY priority ASC, ten ASC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date hsdSQL = rs.getDate("han_su_dung");
                LocalDate hsd = (hsdSQL != null) ? hsdSQL.toLocalDate() : null;
                allSanPhams.add(new SanPhamInfo(rs.getString("ma_san_pham"), rs.getString("ten"), rs.getInt("don_gia"), rs.getInt("so_luong"), hsd));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách sản phẩm gợi ý: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureAutocomplete() {
        JTextField editor = (JTextField) comboBoxSP.getEditor().getEditorComponent();
        comboBoxSP.addActionListener(e -> {
            Object selectedItem = comboBoxSP.getSelectedItem();
            if (e.getActionCommand().equals("comboBoxEdited")) { return; }
            if (selectedItem instanceof SanPhamInfo) {
                SanPhamInfo selectedSP = (SanPhamInfo) selectedItem;
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tfDonGiaBan.setText(formatter.format(selectedSP.getDonGia()) + " VNĐ");
                tfTonKho.setText(String.valueOf(selectedSP.getSoLuongTon()));
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String text = editor.getText();
                    if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_ENTER) { return; }
                    if (text.isEmpty()) { 
                        comboBoxSP.hidePopup(); 
                        tfDonGiaBan.setText(""); 
                        tfTonKho.setText("");
                        return; 
                    }
                    DefaultComboBoxModel<SanPhamInfo> model = new DefaultComboBoxModel<>();
                    for (SanPhamInfo sp : allSanPhams) {
                        if (sp.getTen().toLowerCase().contains(text.toLowerCase())) {
                            model.addElement(sp);
                        }
                    }
                    comboBoxSP.setModel(model);
                    comboBoxSP.setPopupVisible(model.getSize() > 0);
                    editor.setText(text);
                });
            }
        });
    }
    private void loadChiTietSP() {
        modelSP.setRowCount(0);
        String sql = "SELECT ct.ma_san_pham, sp.ten, ct.so_luong, ct.don_gia_nhap FROM chitietnhaphang ct JOIN sanpham sp ON ct.ma_san_pham = sp.ma_san_pham WHERE ct.ma_nhap = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maNhap);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modelSP.addRow(new Object[]{
                        rs.getString("ma_san_pham"), rs.getString("ten"),
                        rs.getInt("so_luong"), rs.getInt("don_gia_nhap"),
                        (long) rs.getInt("so_luong") * rs.getInt("don_gia_nhap")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết đơn hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        int soLuong, donGia;
        try {
            soLuong = Integer.parseInt(tfSoLuong.getText().trim());
            donGia = Integer.parseInt(tfDonGia.getText().trim());
            if (soLuong <= 0 || donGia < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng hoặc đơn giá không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO chitietnhaphang (ma_nhap, ma_san_pham, so_luong, don_gia_nhap) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maNhap); 
            stmt.setString(2, maSP); 
            stmt.setInt(3, soLuong); 
            stmt.setInt(4, donGia);
            stmt.executeUpdate();
            
            // Trigger của CSDL sẽ tự động cộng số lượng và cập nhật tổng tiền

            // Cập nhật giao diện
            if (mainUiRef != null) mainUiRef.refreshKpis();
            JOptionPane.showMessageDialog(this, "Đã thêm sản phẩm thành công!");
            loadChiTietSP();
            loadAllSanPhamData();
            
            // Xóa trường nhập liệu
            SwingUtilities.invokeLater(() -> {
                comboBoxSP.setSelectedItem(null); tfSoLuong.setText(""); tfDonGia.setText("");
                tfDonGiaBan.setText(""); tfTonKho.setText("");
                ((JTextField)comboBoxSP.getEditor().getEditorComponent()).setText("");
            });
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                 JOptionPane.showMessageDialog(this, "Sản phẩm này đã có trong đơn hàng! Hãy xóa và thêm lại.", "Lỗi Trùng lặp", JOptionPane.ERROR_MESSAGE);
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

        String sql = "DELETE FROM chitietnhaphang WHERE ma_nhap = ? AND ma_san_pham = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maNhap); 
            stmt.setString(2, maSP);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Trigger của CSDL sẽ tự động trừ số lượng và cập nhật tổng tiền
                
                // Cập nhật giao diện
                if (mainUiRef != null) mainUiRef.refreshKpis();
                JOptionPane.showMessageDialog(this, "Đã xóa sản phẩm khỏi đơn hàng!");
                loadChiTietSP();
                loadAllSanPhamData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatTongTien() { /* Đã được xử lý bởi Trigger */ }
    
    class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() { setHorizontalAlignment(JLabel.RIGHT); }
        @Override
        public void setValue(Object value) {
            if (value instanceof Number) {
                setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(value) + " VNĐ");
            } else { super.setValue(value); }
        }
    }

    class CenterRenderer extends DefaultTableCellRenderer { public CenterRenderer() { setHorizontalAlignment(JLabel.CENTER); } }
    
    private static class SanPhamInfo {
        private final String maSanPham, ten; 
        private final int donGia, soLuongTon;
        private final LocalDate hanSuDung;
        public SanPhamInfo(String maSanPham, String ten, int donGia, int soLuongTon, LocalDate hanSuDung) {
            this.maSanPham = maSanPham; this.ten = ten; this.donGia = donGia;
            this.soLuongTon = soLuongTon; this.hanSuDung = hanSuDung;
        }
        public String getMaSanPham() { return maSanPham; }
        public String getTen() { return ten; }
        public int getDonGia() { return donGia; }
        public int getSoLuongTon() { return soLuongTon; }
        public LocalDate getHanSuDung() { return hanSuDung; }
        public boolean isWarning() {
            if (soLuongTon < 10) return true;
            if (hanSuDung != null) {
                LocalDate homNay = LocalDate.now();
                return !hanSuDung.isBefore(homNay) && hanSuDung.isBefore(homNay.plusDays(30));
            }
            return false;
        }
        @Override public String toString() { return ten; }
    }
    
    class SanPhamComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof SanPhamInfo) {
                SanPhamInfo sp = (SanPhamInfo) value;
                if (sp.isWarning()) {
                    setBackground(isSelected ? list.getSelectionBackground().darker() : COLOR_WARNING_BG);
                    setForeground(isSelected ? list.getSelectionForeground() : COLOR_WARNING_FG.darker());
                    setText(sp.getTen() + " (Cần nhập gấp!)");
                } else {
                    setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                    setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                }
            }
            return this;
        }
    }
}