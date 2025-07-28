package sanpham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import databaseconnector.DatabaseConnector;
import mainui.MainUI;

public class ThemSanPham extends JDialog {
    // --- Các hằng số cho Style ---
    private static final Font FONT_BASE = new Font("Cambria", Font.PLAIN, 16);
    private static final Font FONT_BOLD = new Font("Cambria", Font.BOLD, 16);
    private static final Color COLOR_BACKGROUND = new Color(245, 248, 250);
    
    private JTextField tfMaSP, tfTen, tfDonVi, tfDonGia, tfHSD, tfSoLuong;
    private JComboBox<String> cbNCC, cbKho;
    private JButton btnSave, btnCancel;
    private Runnable onProductAdded;

    // <<<< THAY ĐỔI: Thêm biến lưu tham chiếu MainUI >>>>
    private MainUI mainUiRef;

    /**
     * <<<< THAY ĐỔI: Sửa constructor để nhận tham chiếu MainUI >>>>
     */
    public ThemSanPham(Frame parent, MainUI mainUi, Runnable onProductAdded) {
        super(parent, "Thêm Sản Phẩm Mới", true);
        this.mainUiRef = mainUi;
        this.onProductAdded = onProductAdded;
        
        setSize(700, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_BACKGROUND);

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        loadNhaCungCap();
        loadKhoHang();
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), " Thông tin sản phẩm "),
            new EmptyBorder(10, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        tfMaSP = new JTextField();
        tfTen = new JTextField();
        tfDonVi = new JTextField();
        tfDonGia = new JTextField();
        tfHSD = new JTextField();
        tfSoLuong = new JTextField();
        cbNCC = new JComboBox<>();
        cbKho = new JComboBox<>();

        int row = 0;
        addLabelAndComponent(form, gbc, row++, "Mã sản phẩm:", tfMaSP);
        addLabelAndComponent(form, gbc, row++, "Tên sản phẩm:", tfTen);
        addLabelAndComponent(form, gbc, row++, "Đơn vị:", tfDonVi);
        addLabelAndComponent(form, gbc, row++, "Đơn giá:", tfDonGia);
        addLabelAndComponent(form, gbc, row++, "Hạn sử dụng (YYYY-MM-DD):", tfHSD);
        addLabelAndComponent(form, gbc, row++, "Số lượng ban đầu:", tfSoLuong);
        addLabelAndComponent(form, gbc, row++, "Nhà cung cấp:", cbNCC);
        addLabelAndComponent(form, gbc, row++, "Kho hàng:", cbKho);

        return form;
    }

    private void addLabelAndComponent(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        JLabel label = new JLabel(labelText);
        label.setFont(FONT_BOLD);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(label, gbc);

        comp.setFont(FONT_BASE);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(comp, gbc);
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(COLOR_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        btnSave = createStyledButton("Lưu");
        btnSave.addActionListener(e -> saveProduct());

        btnCancel = createStyledButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        return buttonPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1), 
            BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
        return button;
    }

    private void loadNhaCungCap() {
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ma_ncc, ten FROM nhacungcap ORDER BY ten")) {
            cbNCC.removeAllItems();
            while (rs.next()) {
                cbNCC.addItem(rs.getString("ma_ncc") + " - " + rs.getString("ten"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nhà cung cấp: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadKhoHang() {
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ma_kho, ten FROM khohang ORDER BY ten")) {
            cbKho.removeAllItems();
            while (rs.next()) {
                cbKho.addItem(rs.getString("ma_kho") + " - " + rs.getString("ten"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải kho hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProduct() {
        String maSP = tfMaSP.getText().trim();
        String ten = tfTen.getText().trim();
        String donVi = tfDonVi.getText().trim();
        String donGiaStr = tfDonGia.getText().trim();
        String hsdStr = tfHSD.getText().trim();
        String soLuongStr = tfSoLuong.getText().trim();
        String selectedNCC = (String) cbNCC.getSelectedItem();
        String selectedKho = (String) cbKho.getSelectedItem();

        if (maSP.isEmpty() || ten.isEmpty() || donGiaStr.isEmpty() || soLuongStr.isEmpty() || selectedNCC == null || selectedKho == null) {
            JOptionPane.showMessageDialog(this, "Mã, Tên, Đơn giá, Số lượng, NCC và Kho không được để trống.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement checkPs = conn.prepareStatement("SELECT 1 FROM sanpham WHERE ma_san_pham = ?");
            checkPs.setString(1, maSP);
            if (checkPs.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Mã sản phẩm đã tồn tại. Vui lòng chọn mã khác.", "Lỗi trùng lặp", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO sanpham(ma_san_pham, ten, don_vi, don_gia, han_su_dung, so_luong, ma_ncc, ma_kho) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, maSP);
            ps.setString(2, ten);
            ps.setString(3, donVi.isEmpty() ? null : donVi);
            ps.setLong(4, Long.parseLong(donGiaStr));
            
            if (hsdStr.isEmpty()) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(hsdStr));
            }
            
            ps.setInt(6, Integer.parseInt(soLuongStr));
            ps.setString(7, selectedNCC.split(" - ")[0]);
            ps.setString(8, selectedKho.split(" - ")[0]);
            
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công.", "Thành Công", JOptionPane.INFORMATION_MESSAGE);

            // <<<< THAY ĐỔI: Gọi lại MainUI để cập nhật KPI >>>>
            if (mainUiRef != null) {
                mainUiRef.refreshKpis();
            }

            if (onProductAdded != null) {
                onProductAdded.run();
            }
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi SQL khi thêm sản phẩm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Định dạng dữ liệu không hợp lệ. Vui lòng kiểm tra lại (ví dụ: ngày YYYY-MM-DD, số là số).", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        }
    }
}