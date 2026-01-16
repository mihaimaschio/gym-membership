using System;
using System.Drawing;
using System.Windows.Forms;
using GymMembershipClient.Services;

namespace GymMembershipClient.Forms
{
    public class LoginForm : Form
    {
        private TextBox _emailBox;
        private TextBox _passwordBox;
        private Button _loginButton;
        private readonly AuthService _authService;
        private readonly ApiClient _apiClient;

        public LoginForm()
        {
            _apiClient = new ApiClient();
            _authService = new AuthService(_apiClient);

            InitializeComponent();
        }

        private void InitializeComponent()
        {
            this.Text = "Login - Gym Membership";
            this.Size = new Size(400, 300);
            this.StartPosition = FormStartPosition.CenterScreen;

            Label emailLabel = new Label { Text = "Email:", Location = new Point(50, 50), AutoSize = true };
            _emailBox = new TextBox { Location = new Point(150, 50), Width = 200 };

            Label passwordLabel = new Label { Text = "Password:", Location = new Point(50, 100), AutoSize = true };
            _passwordBox = new TextBox { Location = new Point(150, 100), Width = 200, PasswordChar = '*' };

            _loginButton = new Button { Text = "Login", Location = new Point(150, 150), Width = 100 };
            _loginButton.Click += LoginButton_Click;

            this.Controls.Add(emailLabel);
            this.Controls.Add(_emailBox);
            this.Controls.Add(passwordLabel);
            this.Controls.Add(_passwordBox);
            this.Controls.Add(_loginButton);
        }

        private async void LoginButton_Click(object sender, EventArgs e)
        {
            _loginButton.Enabled = false;
            try
            {
                var response = await _authService.LoginAsync(_emailBox.Text, _passwordBox.Text);
                MessageBox.Show($"Login Successful! Welcome {response.Email}", "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);
                
                var dashboard = new MemberDashboardForm(_apiClient, response.Email);
                dashboard.Show();
                this.Hide();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Login Failed: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                _loginButton.Enabled = true;
            }
        }
    }
}
