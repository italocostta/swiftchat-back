package com.pd.swiftchat.model;

public enum TipoUsuario {
    USUARIO(1),
    FUNCIONARIO(2);

    private final int codigo;

    TipoUsuario(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static TipoUsuario fromCodigo(int codigo) {
        for (TipoUsuario tipo : TipoUsuario.values()) {
            if (tipo.getCodigo() == codigo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de TipoUsuario inválido: " + codigo);
    }
}
