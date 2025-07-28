package sanpham;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import databaseconnector.DatabaseConnector;
import mainui.MainUI;

public class ChinhSuaSanPham extends JDialog {
    private JTextField tfTen, tfDonVi, tfDonGia, tfHSD, tfSoLuong, tfMaKho;
    private final String maSP;
    private final Runnable onSuccess;
    private final Font cambriaFont = new Font("Cambria", Font.PLAIN, 19);

    // <<<< THAY ĐỔI 1: Thêm biến lưu tham chiếu MainUI >>>>
    private MainUI mainUiRef;

    /**
     * <<<< THAY ĐỔI 2: Sửa constructor để nhận tham chiếu MainUI >>>>
     */
    public ChinhSuaSanPham(Frame parent, MainUI mainUi, String maSP, Runnable onSuccess) {
        super(parent, "Chỉnh sửa sản phẩm", true);
        this.mainUiRef = mainUi;
        this.maSP = maSP;
        this.onSuccess = onSuccess;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTen = new JLabel("Tên sản phẩm:");
        tfTen = new JTextField(20);
        addToPanel(panel, lblTen, tfTen, gbc, 0);

        JLabel lblDonVi = new JLabel("Đơn vị tính:");
        tfDonVi = new JTextField(20);
        addToPanel(panel, lblDonVi, tfDonVi, gbc, 1);

        JLabel lblDonGia = new JLabel("Đơn giá:");
        tfDonGia = new JTextField(20);
        addToPanel(panel, lblDonGia, tfDonGia, gbc, 2);

        JLabel lblHSD = new JLabel("HSD (yyyy-mm-dd):");
        tfHSD = new JTextField(20);
        addToPanel(panel, lblHSD, tfHSD, gbc, 3);

        JLabel lblSoLuong = new JLabel("Số lượng:");
        tfSoLuong = new JTextField(20);
        addToPanel(panel, lblSoLuong, tfSoLuong, gbc, 4);

        JLabel lblMaKho = new JLabel("Mã kho:");
        tfMaKho = new JTextField(20);
        addToPanel(panel, lblMaKho, tfMaKho, gbc, 5);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.setFont(cambriaFont);
        btnCancel.setFont(cambriaFont);

        btnSave.addActionListener(e -> updateSanPham());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(buttonPanel, gbc);

        setFontRecursive(panel);

        add(panel);
        pack();
        setLocationRelativeTo(parent);

        loadSanPham();
    }

    private void setFontRecursive(Component component) {
        component.setFont(cambriaFont);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setFontRecursive(child);
            }
        }
    }

    private void addToPanel(JPanel panel, JLabel label, JTextField textField, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }

    private void loadSanPham() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM sanpham WHERE ma_san_pham = ?");
            ps.setString(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfTen.setText(rs.getString("ten"));
                tfDonVi.setText(rs.getString("don_vi"));
                tfDonGia.setText(rs.getString("don_gia"));
                tfHSD.setText(rs.getString("han_su_dung"));
                tfSoLuong.setText(rs.getString("so_luong"));
                tfMaKho.setText(rs.getString("ma_kho"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void updateSanPham() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE sanpham SET ten=?, don_vi=?, don_gia=?, han_su_dung=?, so_luong=?, ma_kho=? WHERE ma_san_pham=?");
            ps.setString(1, tfTen.getText().trim());
            ps.setString(2, tfDonVi.getText().trim());
            ps.setInt(3, Integer.parseInt(tfDonGia.getText().trim()));
            ps.setDate(4, Date.valueOf(tfHSD.getText().trim()));
            ps.setInt(5, Integer.parseInt(tfSoLuong.getText().trim()));
            ps.setString(6, tfMaKho.getText().trim());
            ps.setString(7, maSP);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công.");

                // <<<< THAY ĐỔI 3: Gọi lại MainUI để cập nhật KPI >>>>
                if (mainUiRef != null) {
                    mainUiRef.refreshKpis();
                }

                onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm.");
            }
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());
        }
    }
}