package donxuathang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import mainui.MainUI; // <<<< THÊM IMPORT

public class ThemKhachHangDialog extends JDialog {
    // --- Các hằng số Style ---
    private static final Font FONT_BASE = new Font("Cambria", Font.PLAIN, 16);
    private static final Font FONT_BOLD = new Font("Cambria", Font.BOLD, 16);
    private static final Color COLOR_BACKGROUND = new Color(245, 248, 250);

    private JTextField tfMaKhach, tfTen, tfSoDienThoai;
    private JButton btnLuu, btnHuy;
    private boolean succeeded = false;
    private Connection conn;
    
    // <<<< THAY ĐỔI 1: Thêm biến lưu tham chiếu MainUI >>>>
    private MainUI mainUiRef;

    // <<<< THAY ĐỔI 4: Thêm biến để trả về tên khách hàng mới >>>>
    private String tenKhachMoi;
    
    /**
     * <<<< THAY ĐỔI 2: Sửa constructor để nhận tham chiếu MainUI >>>>
     */
    public ThemKhachHangDialog(Dialog parent, Connection conn, MainUI mainUi) {
        super(parent, "Thêm Khách Hàng Mới", true);
        this.conn = conn;
        this.mainUiRef = mainUi; // Lưu tham chiếu

        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_BACKGROUND);

        // Panel nhập liệu
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        inputPanel.setBackground(COLOR_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Mã khách
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        inputPanel.add(createStyledLabel("Mã Khách:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tfMaKhach = new JTextField(20); tfMaKhach.setFont(FONT_BASE);
        inputPanel.add(tfMaKhach, gbc);

        // Tên khách
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        inputPanel.add(createStyledLabel("Tên Khách:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tfTen = new JTextField(20); tfTen.setFont(FONT_BASE);
        inputPanel.add(tfTen, gbc);

        // Số điện thoại
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        inputPanel.add(createStyledLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tfSoDienThoai = new JTextField(20); tfSoDienThoai.setFont(FONT_BASE);
        inputPanel.add(tfSoDienThoai, gbc);

        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(COLOR_BACKGROUND);
        btnLuu = createStyledButton("Lưu");
        btnHuy = createStyledButton("Hủy");
        buttonPanel.add(btnLuu);
        buttonPanel.add(btnHuy);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        btnLuu.addActionListener(e -> luuKhachHang());
        btnHuy.addActionListener(e -> dispose());
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(Color.WHITE); }
        });
        return button;
    }

    private void luuKhachHang() {
        String ma = tfMaKhach.getText().trim();
        String ten = tfTen.getText().trim();
        String sdt = tfSoDienThoai.getText().trim();

        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mã và tên khách hàng.");
            return;
        }

        try(Connection localConn = databaseconnector.DatabaseConnector.getConnection()) { // Tạo connection mới để không xung đột
            PreparedStatement checkPs = localConn.prepareStatement("SELECT 1 FROM khachhang WHERE ma_khach = ?");
            checkPs.setString(1, ma);
            if (checkPs.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Mã khách hàng đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            PreparedStatement ps = localConn.prepareStatement("INSERT INTO khachhang (ma_khach, ten, so_dien_thoai) VALUES (?, ?, ?)");
            ps.setString(1, ma);
            ps.setString(2, ten);
            ps.setString(3, sdt.isEmpty() ? null : sdt);
            ps.executeUpdate();

            // <<<< THAY ĐỔI 3: Gọi lại MainUI để cập nhật KPI (nếu cần) >>>>
            // Hiện tại việc thêm khách hàng chưa ảnh hưởng KPI, nhưng có thể sau này sẽ cần.
            if (mainUiRef != null) {
                // mainUiRef.refreshKpis(); // Bạn có thể bỏ comment dòng này nếu cần
            }

            succeeded = true;
            this.tenKhachMoi = ten; // Lưu lại tên khách hàng mới
            JOptionPane.showMessageDialog(this, "Đã thêm khách hàng thành công!");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm khách hàng: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    // <<<< THAY ĐỔI 4: Phương thức để dialog cha lấy tên khách mới >>>>
    public String getTenKhachMoi() {
        return tenKhachMoi;
    }
}