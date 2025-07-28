// File: khohang/NhapThongTinSanPhamDialog.java
package khohang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class NhapThongTinSanPhamDialog extends JDialog {
    // Các hằng số giao diện để đồng bộ
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_VALUE = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 14);
    private static final Color COLOR_PRIMARY_TEXT = new Color(51, 51, 51);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_HEADER_TEXT = new Color(34, 34, 34);


    private JTextField txtGiaNhap;
    private JSpinner spinnerSoLuong;
    private boolean confirmed = false;

    // Constructor nhận vào thông tin sản phẩm, bao gồm cả giá bán hiện tại
    public NhapThongTinSanPhamDialog(Frame owner, String tenSP, int giaBanHienTai) {
        super(owner, "Thêm Sản Phẩm vào DS Chờ", true);

        // --- GIAO DIỆN ĐƯỢC THIẾT KẾ LẠI ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(COLOR_HEADER_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // Khoảng cách giữa các component
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;


        // 1. Tên Sản Phẩm (chỉ hiển thị)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel(tenSP);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_HEADER_TEXT);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridy++;
        mainPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(15, 5, 8, 5);

        // 2. Giá Bán Hiện Tại (chỉ hiển thị)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        mainPanel.add(createLabel("Giá bán hiện tại:"), gbc);

        gbc.gridx = 1;
        JLabel lblGiaBanValue = new JLabel(formatCurrency(giaBanHienTai));
        lblGiaBanValue.setFont(new Font("Arial", Font.ITALIC, 16));
        lblGiaBanValue.setForeground(Color.BLUE);
        mainPanel.add(lblGiaBanValue, gbc);

        // 3. Giá Nhập (cho người dùng nhập)
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(createLabel("Giá nhập mong muốn:"), gbc);

        gbc.gridx = 1;
        txtGiaNhap = new JTextField(15);
        txtGiaNhap.setFont(FONT_VALUE);
        mainPanel.add(txtGiaNhap, gbc);

        // 4. Số Lượng (cho người dùng nhập)
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(createLabel("Số lượng nhập:"), gbc);

        gbc.gridx = 1;
        spinnerSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spinnerSoLuong.setFont(FONT_VALUE);
        mainPanel.add(spinnerSoLuong, gbc);

        // --- Các nút bấm ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton btnConfirm = createStyledButton("Thêm vào DS", COLOR_HEADER_TEXT, Color.WHITE);
        btnConfirm.addActionListener(e -> onConfirm());

        JButton btnCancel = createStyledButton("Hủy", new Color(178, 34, 34), Color.WHITE);
        btnCancel.addActionListener(e -> onCancel());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack(); // Tự động điều chỉnh kích thước cửa sổ
        setLocationRelativeTo(owner);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_PRIMARY_TEXT);
        return label;
    }
    
    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 20, 8, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    private void onConfirm() {
        try {
            int giaNhap = Integer.parseInt(txtGiaNhap.getText().trim());
            int soLuong = (int) spinnerSoLuong.getValue();

            if (giaNhap < 0 || soLuong <= 0) {
                JOptionPane.showMessageDialog(this, "Giá nhập và số lượng phải lớn hơn 0.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            setVisible(false);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giá nhập là một con số hợp lệ.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            txtGiaNhap.requestFocus();
        }
    }

    private void onCancel() {
        confirmed = false;
        setVisible(false);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getGiaNhap() {
        return Integer.parseInt(txtGiaNhap.getText().trim());
    }

    public int getSoLuong() {
        return (int) spinnerSoLuong.getValue();
    }
}