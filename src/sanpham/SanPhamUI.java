package sanpham;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import databaseconnector.DatabaseConnector;
import mainui.MainUI;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class SanPhamUI extends JFrame {
    // <<<< BẢNG MÀU VÀ FONT MỚI, ĐỒNG BỘ VỚI MAINUI >>>>
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250); // Màu xanh dương rất nhạt
    private static final Color COLOR_LOW_STOCK_BG = new Color(255, 244, 230); // Màu be/vàng nhạt

    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearchTenSP;
    private JCheckBox cbSapHetHan;
    private Connection conn;
    private MainUI mainUiRef;

    public SanPhamUI(MainUI mainUi) {
        this.mainUiRef = mainUi;
        conn = DatabaseConnector.getConnection();
        
        setTitle("Quản Lý Sản Phẩm");
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
        loadData();
    }

    private void initUI() {
        Container contentPane = getContentPane();
        contentPane.setBackground(COLOR_BACKGROUND);
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPane.add(createMainContentPanel(), BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel titleLabel = new JLabel("QUẢN LÝ SẢN PHẨM");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // --- Nút Quay về ---
        JButton btnBack = createStyledButton("Quay về");
        btnBack.addActionListener(e -> dispose());
        
        JPanel backButtonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backButtonContainer.setOpaque(false);
        backButtonContainer.add(btnBack);

        headerPanel.add(backButtonContainer, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 20));
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));

        // Thanh công cụ (lọc và nút hành động)
        mainContentPanel.add(createToolbarPanel(), BorderLayout.NORTH);

        // Bảng dữ liệu
        mainContentPanel.add(createTablePanel(), BorderLayout.CENTER);

        return mainContentPanel;
    }
    
    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new BorderLayout(20, 10));
        toolbarPanel.setOpaque(false);

        // --- Phần Lọc (Bên trái) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(FONT_BUTTON);
        searchLabel.setForeground(COLOR_SECONDARY_TEXT);
        filterPanel.add(searchLabel);
        
        tfSearchTenSP = new JTextField(25);
        tfSearchTenSP.setFont(FONT_TABLE_CELL);
        filterPanel.add(tfSearchTenSP);

        cbSapHetHan = new JCheckBox("Sản phẩm sắp hết hạn");
        cbSapHetHan.setFont(FONT_BUTTON);
        cbSapHetHan.setOpaque(false);
        cbSapHetHan.setForeground(COLOR_SECONDARY_TEXT);
        filterPanel.add(cbSapHetHan);

        toolbarPanel.add(filterPanel, BorderLayout.WEST);

        // --- Phần Nút Hành động (Bên phải) ---
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonPanel.setOpaque(false);

        JButton btnAdd = createStyledButton("Thêm Sản Phẩm");
        btnAdd.setBackground(COLOR_HEADER_TEXT); // Nút chính có màu nền đậm
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            ThemSanPham dialog = new ThemSanPham(this, mainUiRef, this::loadData);
            dialog.setVisible(true);
        });

        JButton btnDelete = createStyledButton("Xóa Sản Phẩm");
        btnDelete.addActionListener(e -> deleteSelectedProduct());

        actionButtonPanel.add(btnAdd);
        actionButtonPanel.add(btnDelete);

        toolbarPanel.add(actionButtonPanel, BorderLayout.EAST);
        
        // Gán sự kiện cho bộ lọc
        tfSearchTenSP.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        cbSapHetHan.addActionListener(e -> loadData());
        
        return toolbarPanel;
    }

    private JScrollPane createTablePanel() {
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        
        styleTable(); // Áp dụng style mới

        String[] columns = {"Mã SP", "Tên SP", "Đơn vị", "Đơn giá", "HSD", "Tồn kho", "Kho", "Nhà cung cấp"};
        tableModel.setColumnIdentifiers(columns);
        
        setupTableColumnWidths();

        // Mở dialog sửa khi double-click
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    String maSP = table.getValueAt(table.getSelectedRow(), 0).toString();
                    ChinhSuaSanPham dialog = new ChinhSuaSanPham(SanPhamUI.this, mainUiRef, maSP, SanPhamUI.this::loadData);
                    dialog.setVisible(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER)); // Viền cho bảng
        return scrollPane;
    }

    private void styleTable() {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(40);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_PRIMARY_TEXT);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_SECONDARY_TEXT);
        header.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        table.setDefaultRenderer(Object.class, new SanPhamTableCellRenderer());
    }
    
    // ... các phương thức khác không đổi, trừ style nút ...

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
        // Loại bỏ MouseListener cũ để có thể set màu nền khác nhau cho từng nút
        return button;
    }
    
    private void deleteSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sản phẩm để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maSP = tableModel.getValueAt(selectedRow, 0).toString();
        try {
            if (isProductInUse(maSP)) {
                JOptionPane.showMessageDialog(this, "Không thể xóa sản phẩm này!\nSản phẩm đã tồn tại trong các đơn hàng nhập hoặc xuất.", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sản phẩm '" + maSP + "' không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM sanpham WHERE ma_san_pham = ?");
                ps.setString(1, maSP);
                if (ps.executeUpdate() > 0) {
                    if (mainUiRef != null) mainUiRef.refreshKpis();
                    JOptionPane.showMessageDialog(this, "Xóa sản phẩm thành công.");
                    loadData();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // --- Các phương thức còn lại không có thay đổi logic lớn ---
    private void setupTableColumnWidths() {
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(400);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(4).setPreferredWidth(150);
        columnModel.getColumn(5).setPreferredWidth(100);
        columnModel.getColumn(6).setPreferredWidth(200);
        columnModel.getColumn(7).setPreferredWidth(200);
    }
    public void loadData() {
        String tenSPFilter = tfSearchTenSP.getText().trim();
        boolean sapHetHan = cbSapHetHan.isSelected();
        String sql = "SELECT sp.ma_san_pham, sp.ten AS ten_sp, sp.don_vi, sp.don_gia, sp.han_su_dung, sp.so_luong, kho.ten AS ten_kho, ncc.ten AS ten_ncc FROM sanpham sp LEFT JOIN khohang kho ON sp.ma_kho = kho.ma_kho LEFT JOIN nhacungcap ncc ON sp.ma_ncc = ncc.ma_ncc WHERE 1=1";
        if (!tenSPFilter.isEmpty()) { sql += " AND sp.ten ILIKE ?"; }
        if (sapHetHan) { sql += " AND sp.han_su_dung BETWEEN current_date AND current_date + INTERVAL '30 days'"; }
        sql += " ORDER BY sp.so_luong ASC, sp.ten ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (!tenSPFilter.isEmpty()) { ps.setString(paramIndex++, "%" + tenSPFilter + "%"); }
            ResultSet rs = ps.executeQuery();
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("ma_san_pham"), rs.getString("ten_sp"), rs.getString("don_vi"), rs.getLong("don_gia"), rs.getDate("han_su_dung"), rs.getInt("so_luong"), rs.getString("ten_kho"), rs.getString("ten_ncc")});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean isProductInUse(String maSP) throws SQLException {
        String checkNhapSQL = "SELECT 1 FROM chitietnhaphang WHERE ma_san_pham = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkNhapSQL)) {
            ps.setString(1, maSP);
            if (ps.executeQuery().next()) return true;
        }
        String checkXuatSQL = "SELECT 1 FROM chitietxuat WHERE ma_san_pham = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkXuatSQL)) {
            ps.setString(1, maSP);
            if (ps.executeQuery().next()) return true;
        }
        return false;
    }
    
    /**
     * Renderer mới cho bảng, sử dụng màu sắc tinh tế hơn.
     */
    class SanPhamTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(JLabel.CENTER);
            setForeground(COLOR_PRIMARY_TEXT);

            if (column == 1) setHorizontalAlignment(JLabel.LEFT); // Căn trái cho tên sản phẩm

            // Định dạng tiền tệ
            if (column == 3 && value instanceof Number) {
                setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(value));
            } else {
                setText(value != null ? value.toString() : "");
            }

            // Tô màu nền cho dòng
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                int soLuong = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), 5);
                if (soLuong < 10) { // Tô màu cảnh báo cho hàng tồn kho thấp
                    setBackground(COLOR_LOW_STOCK_BG);
                } else {
                    setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
                }
            }
            
            return this;
        }
    }
}