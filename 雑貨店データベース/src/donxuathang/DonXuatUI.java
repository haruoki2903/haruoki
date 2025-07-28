package donxuathang;

import javax.swing.*;
import javax.swing.table.*;
import chitietxuathang.ChiTietXuatUI;
import mainui.MainUI;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import databaseconnector.DatabaseConnector;

public class DonXuatUI extends JFrame {
    private static final Color COLOR_BACKGROUND = new Color(248, 248, 248);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);
    private static final Color COLOR_BORDER = new Color(224, 224, 224);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_SECONDARY_TEXT = new Color(102, 102, 102);
    private static final Color COLOR_SELECTION = new Color(220, 235, 250);
    private static final Color COLOR_ARCHIVED_ROW_BG = new Color(245, 245, 245);
    private static final Color COLOR_ARCHIVED_ROW_FG = new Color(150, 150, 150);

    private static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTable table;
    private DefaultTableModel model;
    private Connection conn;
    private MainUI mainUiRef;

    public DonXuatUI(MainUI mainUi) {
        this.mainUiRef = mainUi;
        conn = DatabaseConnector.getConnection();
        
        setTitle("Quản Lý Đơn Xuất Hàng");
        setSize(1280, 800);
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
        refreshData();
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

        JLabel titleLabel = new JLabel("DANH SÁCH ĐƠN XUẤT HÀNG");
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton btnTaoMoi = createStyledButton("Tạo Đơn Mới");
        btnTaoMoi.setBackground(COLOR_HEADER_TEXT);
        btnTaoMoi.setForeground(Color.WHITE);
        btnTaoMoi.addActionListener(e -> taoDonXuatMoi());
        
        JButton btnXoa = createStyledButton("Xóa Đơn Đã Chọn");
        btnXoa.addActionListener(e -> xoaDonXuatDaChon());
        
        JButton btnQuayVeMain = createStyledButton("Quay về");
        btnQuayVeMain.addActionListener(e -> dispose());
        
        buttonPanel.add(btnTaoMoi);
        buttonPanel.add(btnXoa);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(btnQuayVeMain);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));
        
        JLabel instructionLabel = new JLabel("Double-click vào một dòng để xem hoặc chỉnh sửa chi tiết đơn hàng. Các đơn màu xám đã được quyết toán.");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        instructionLabel.setForeground(COLOR_SECONDARY_TEXT);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 0));
        
        mainContentPanel.add(instructionLabel, BorderLayout.NORTH);
        mainContentPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        return mainContentPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Mã Xuất", "Ngày Xuất", "Khách Hàng", "Tổng Tiền", "Trạng Thái"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        String maXuat = model.getValueAt(selectedRow, 0).toString();
                        new ChiTietXuatUI(mainUiRef, DonXuatUI.this, maXuat).setVisible(true);
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        return scrollPane;
    }
    
    private void styleTable() {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(40);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_SELECTION);
        table.setSelectionForeground(COLOR_PRIMARY_TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(COLOR_HEADER_BG);
        header.setForeground(COLOR_SECONDARY_TEXT);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        table.setDefaultRenderer(Object.class, new ArchivedRowRenderer());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(COLOR_HEADER_BG);
        button.setForeground(COLOR_PRIMARY_TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), 
            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        return button;
    }

    public void refreshData() {
        model.setRowCount(0);
        String sql = "SELECT dx.ma_xuat, dx.ngay_xuat, kh.ten, dx.tong_tien, dx.ma_ky " +
                     "FROM donxuat dx LEFT JOIN khachhang kh ON dx.ma_khach = kh.ma_khach " +
                     "ORDER BY dx.ngay_xuat DESC, dx.ma_xuat DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String trangThai = (rs.getString("ma_ky") == null) ? "Chưa quyết toán" : "Đã quyết toán";
                model.addRow(new Object[]{
                    rs.getString("ma_xuat"), rs.getDate("ngay_xuat"),
                    rs.getString("ten"), rs.getLong("tong_tien"),
                    trangThai
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu đơn xuất: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taoDonXuatMoi() {
        TaoDonXuatDialog dialog = new TaoDonXuatDialog(this, conn, mainUiRef);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            refreshData();
            new ChiTietXuatUI(mainUiRef, this, dialog.getMaXuatMoi()).setVisible(true);
        }
    }
    
    private void xoaDonXuatDaChon() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn xuất để xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maXuat = (String) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đơn xuất " + maXuat + "?\nHành động này sẽ hoàn trả lại số lượng đã xuất.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Chỉ cần DELETE, trigger sẽ lo phần còn lại
        String sql = "DELETE FROM donxuat WHERE ma_xuat = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maXuat);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Xóa đơn xuất thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            
            if(mainUiRef != null) mainUiRef.refreshKpis();
            refreshData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa đơn xuất: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ArchivedRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.CENTER);
            if (column == 2) {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            if (column == 3) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(value));
                }
            } else if (value instanceof Date) {
                 setText(((Date)value).toLocalDate().format(DATE_FORMATTER));
            } else {
                 setText(value == null ? "" : value.toString());
            }

            String trangThai = (String) table.getModel().getValueAt(row, 4);
            if ("Đã quyết toán".equals(trangThai)) {
                if (isSelected) {
                    setBackground(COLOR_SELECTION.darker());
                    setForeground(COLOR_ARCHIVED_ROW_FG.brighter());
                } else {
                    setBackground(COLOR_ARCHIVED_ROW_BG);
                    setForeground(COLOR_ARCHIVED_ROW_FG);
                }
            } else {
                if (isSelected) {
                    setBackground(COLOR_SELECTION);
                    setForeground(COLOR_PRIMARY_TEXT);
                } else {
                    setBackground(row % 2 == 0 ? COLOR_HEADER_BG : COLOR_BACKGROUND);
                    setForeground(COLOR_PRIMARY_TEXT);
                }
            }
            
            return this;
        }
    }
}